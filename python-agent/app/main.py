"""
ACG Space python-agent 服务入口（FastAPI，无状态 AI 大脑）。

Phase 0：打通 SSE 管线（/chat 暂为 echo 流式桩）。
Phase 2：/chat 接入 RAG 检索 + 流式 LLM（DeepSeek chat / 通义 embedding）。
Phase 3：/chat 接入只读 Bangumi 工具（function calling）+ TTL 缓存；工具失败回退 RAG。
"""
import json
import os
import re

from fastapi import FastAPI
from fastapi.responses import StreamingResponse

from app.config import settings
from app.llm import chat_completion, chat_stream, embed
from app.prompts import SYSTEM_PROMPT
from app.rag import build_corpus
from app.schemas import ChatRequest
from app.tools.bangumi import get_airing_now, get_bangumi_detail, search_bangumi
from app.tools.registry import TOOLS

app = FastAPI(title="ACG Space Agent", version="0.3.0")

_BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
_CORPUS_DIR = os.path.join(_BASE_DIR, "corpus")

# 启动时构建语料（向量化）。缺 embedding Key 时此处会抛错，由 uvicorn 启动失败暴露，便于及时发现配置缺失。
corpus = build_corpus(_CORPUS_DIR, embed)


def _sse(obj: dict) -> str:
    """构造 SSE data 帧（与前端 useAgentApi 的解析约定一致：data:{json}\\n\\n）。"""
    return f"data:{json.dumps(obj, ensure_ascii=False)}\n\n"


def _parse_longcat_tool_calls(content: str) -> list[tuple[str, dict]]:
    """从 LongCat 自定义 XML 格式里抠出工具调用。

    LongCat（LLM_CHAT_MODEL）不支持 OpenAI 标准 function-calling 的 tool_calls 字段，
    而是把工具调用写成自有的 XML 标记，直接塞进回复 content：
        <longcat_tool_call>search_bangumi
        <longcat_arg_key>keyword</longcat_arg_key>
        <longcat_arg_value>推理</longcat_arg_value>
        <longcat_arg_key>limit</longcat_arg_key>
        <longcat_arg_value>8</longcat_arg_value>
        </longcat_tool_call>

    返回 [(工具名, 参数字典), ...]；无 XML 时返回空列表。
    """
    calls: list[tuple[str, dict]] = []
    for block in re.finditer(
        r"<longcat_tool_call>(.*?)</longcat_tool_call>", content, re.DOTALL
    ):
        inner = block.group(1)
        lines = [ln.strip() for ln in inner.strip().splitlines() if ln.strip()]
        if not lines:
            continue
        name = lines[0]  # 工具名在首行
        args: dict = {}
        for km, vm in re.finditer(
            r"<longcat_arg_key>(.*?)</longcat_arg_key>\s*"
            r"<longcat_arg_value>(.*?)</longcat_arg_value>",
            inner,
            re.DOTALL,
        ):
            args[km.group(1).strip()] = vm.group(1).strip()
        calls.append((name, args))
    return calls


def _run_tool(name: str, args: dict) -> dict:
    """分发只读 Bangumi 工具；任何异常都转为 error 结果，交由 LLM 回退说明。"""
    try:
        if name == "search_bangumi":
            return search_bangumi(**args)
        if name == "get_bangumi_detail":
            return get_bangumi_detail(**args)
        if name == "get_airing_now":
            return get_airing_now()
    except Exception as e:  # noqa: BLE001
        return {"error": f"工具 {name} 调用失败：{e}"}
    return {"error": f"未知工具：{name}"}


@app.get("/health")
def health():
    return {"status": "ok", "chunks": len(corpus.chunks)}


@app.post("/rebuild")
def rebuild():
    """重新构建 RAG 索引（规则/FAQ/番剧快照变更后调用，或重启服务）。"""
    global corpus
    corpus = build_corpus(_CORPUS_DIR, embed)
    return {"status": "rebuilt", "chunks": len(corpus.chunks)}


@app.post("/chat")
async def chat(req: ChatRequest):
    """SSE 流式对话：RAG 检索 → 拼系统提示 → LLM（带 Bangumi 工具）→ 工具执行 → 流式回答。"""

    async def event_stream():
        try:
            # 1) 检索相关分块
            chunks = corpus.search(req.message, top_k=5)
            context = "\n\n".join(
                f"[来源:{c.source} / {c.title}]\n{c.text}" for c in chunks
            )
            messages = [
                {"role": "system", "content": SYSTEM_PROMPT.format(context=context)}
            ]
            for h in req.history:
                messages.append({"role": h.role, "content": h.content})
            messages.append({"role": "user", "content": req.message})

            # 2) 首轮：带 tools，非流式，检测 tool_calls
            first = chat_completion(messages, tools=TOOLS)
            assistant_msg = first.choices[0].message
            tool_calls = getattr(assistant_msg, "tool_calls", None)

            if tool_calls:
                # 回传 assistant 的 tool_calls 消息（OpenAI 要求）
                yield _sse({"type": "tool_status", "content": "正在查询番剧库…"})
                messages.append(
                    {
                        "role": "assistant",
                        "content": assistant_msg.content or "",
                        "tool_calls": [tc.model_dump() for tc in tool_calls],
                    }
                )
                for tc in tool_calls:
                    args = json.loads(tc.function.arguments or "{}")
                    result = _run_tool(tc.function.name, args)
                    messages.append(
                        {
                            "role": "tool",
                            "tool_call_id": tc.id,
                            "content": json.dumps(result, ensure_ascii=False),
                        }
                    )
                yield _sse({"type": "tool_status", "content": ""})

            elif assistant_msg.content:
                # LongCat 自定义 XML 工具调用兜底：模型把工具调用写成
                # <longcat_tool_call>...</longcat_tool_call> 文本而非 OpenAI tool_calls 字段。
                longcat_calls = _parse_longcat_tool_calls(assistant_msg.content)
                if longcat_calls:
                    # 清空原始 XML 内容，避免它作为「回答」流到前端
                    yield _sse({"type": "tool_status", "content": "正在查询番剧库…"})
                    assistant_msg.content = ""
                    tool_blocks = []
                    for name, args in longcat_calls:
                        result = _run_tool(name, args)
                        tool_blocks.append(
                            f"[工具 {name} 返回]\n{json.dumps(result, ensure_ascii=False)}"
                        )
                    # 非原生 tool-calling 模型：把工具结果作为 user 消息回填，
                    # 要求模型直接基于结果作答（不要重复调用、不要输出 XML）。
                    messages.append(
                        {
                            "role": "user",
                            "content": (
                                "以下是你刚才请求的工具调用的返回结果，请基于它直接回答用户的问题"
                                "（不要重复调用工具，也不要输出任何 XML 标签）：\n\n"
                                + "\n\n".join(tool_blocks)
                            ),
                        }
                    )
                    yield _sse({"type": "tool_status", "content": ""})

            # 3) 流式最终回答（无论是否经过工具，统一走流式）
            for token in chat_stream(messages):
                yield _sse({"type": "token", "content": token})
        except RuntimeError as e:
            # 配置缺失（缺 API Key）等可预期错误：明确提示，便于排查
            yield _sse({"type": "error", "content": f"配置或依赖缺失：{e}"})
        except Exception as e:  # noqa: BLE001
            # 其余异常兜底，保证流正常结束
            yield _sse({"type": "error", "content": f"服务暂不可用：{e}"})
        yield _sse({"type": "done", "content": ""})

    return StreamingResponse(event_stream(), media_type="text/event-stream")
