"""
ACG Space python-agent 服务入口（FastAPI，无状态 AI 大脑）。

Phase 0：打通 SSE 管线（/chat 暂为 echo 流式桩）。
Phase 2：/chat 接入 RAG 检索 + 流式 LLM（DeepSeek chat / 通义 embedding）。
Phase 3：/chat 接入只读 Bangumi 工具（function calling）+ TTL 缓存；工具失败回退 RAG。
"""
import json
import os

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
