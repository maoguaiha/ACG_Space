"""
ACG Space python-agent 服务入口（FastAPI，无状态 AI 大脑）。

Phase 0：打通 SSE 管线（/chat 暂为 echo 流式桩）。
Phase 2：/chat 接入 RAG 检索 + 流式 LLM（DeepSeek chat / 通义 embedding）。
Phase 3：/chat 接入只读 Bangumi 工具（function calling）+ TTL 缓存；工具失败回退 RAG。
"""
import json
import os
import re
import threading
import time

from fastapi import FastAPI
from fastapi.responses import StreamingResponse

from app.config import settings
from app.llm import chat_completion, chat_stream, embed
from app.prompts import SYSTEM_PROMPT
from app.rag import build_corpus
from app.anime_loader import ensure_anime_json
from app.schemas import ChatRequest
from app.tools.bangumi import get_airing_now, get_bangumi_detail, search_bangumi
from app.tools.registry import TOOLS

app = FastAPI(title="ACG Space Agent", version="0.3.0")

# 长对话上下文压缩：单边历史保留的最大消息条数（约 8 轮对话）。
# 超出部分在 main.chat 中截断并以系统备注概括，避免逼近 token 上限。
_MAX_HISTORY_MESSAGES = 16

_BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
_CORPUS_DIR = os.path.join(_BASE_DIR, "corpus")

# 尝试从后端 API 拉取番剧数据生成 anime.json（免手动部署语料文件）
try:
    backend_url = os.getenv("BACKEND_URL", "http://backend:8080")
    ensure_anime_json(backend_url)
except Exception as e:
    print(f"[startup] 拉取番剧语料失败（{e}），跳过番剧知识源")

# 启动时尝试构建语料（向量化）。embedding 不可达时降级为 None，服务仍 healthy，
# 由后台 watcher 重试或首次请求时懒加载重建，避免启动即崩导致健康检查失败。
try:
    corpus = build_corpus(_CORPUS_DIR, embed)
except Exception as e:  # noqa: BLE001
    print(f'[startup] 语料构建失败（embedding 暂不可达），服务仍以无语料状态启动：{e}')
    corpus = None


def _corpus_dir_mtime() -> float:
    """取语料目录下所有文件的最新修改时间（用于热更新检测）。"""
    mtime = 0.0
    for root, _dirs, files in os.walk(_CORPUS_DIR):
        for f in files:
            try:
                mtime = max(mtime, os.path.getmtime(os.path.join(root, f)))
            except OSError:
                pass
    return mtime


def _start_corpus_watcher(interval: int = 30) -> None:
    """后台守护线程：轮询语料目录 mtime，变更后自动重建索引（无需手动 /rebuild 或重启）。

    用标准库轮询而非 watchdog，避免额外依赖。
    """
    global corpus
    last_mtime = _corpus_dir_mtime()

    def _loop() -> None:
        nonlocal last_mtime
        while True:
            time.sleep(interval)
            try:
                m = _corpus_dir_mtime()
                if m > last_mtime:
                    last_mtime = m
                    new_corpus = build_corpus(_CORPUS_DIR, embed)
                    corpus = new_corpus
                    print(f"[corpus-watcher] 语料已热更新，chunks={len(corpus.chunks)}")
            except Exception as e:  # noqa: BLE001
                print(f"[corpus-watcher] 重建失败：{e}")

    threading.Thread(target=_loop, daemon=True).start()


_start_corpus_watcher()


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


def _parse_agnes_tool_calls(content: str) -> list[tuple[str, dict]]:
    """从 Agnes AI 的 XML 格式里抠出工具调用。

    Agnes AI（LLM_CHAT_MODEL）也不走 OpenAI 标准 tool_calls,而是把工具调用写成
    自有 XML 标签直接塞进 content:
        <tool_call>
        <function=get_bangumi_detail>
        <parameter=id>
        274613
        </parameter>
        </function>
        </tool_call>

    返回 [(工具名, 参数字典), ...];无 XML 时返回空列表。
    """
    calls: list[tuple[str, dict]] = []
    # 预处理：剥离常见零宽/不可见字符(ZWSP/ZWNJ/ZWJ/BOM/LRM/RLM)
    # 让 XML 标签格式微调也能匹配
    _ZERO_WIDTH = "\u200b\u200c\u200d\ufeff\u200e\u200f"
    content = content.translate(str.maketrans("", "", _ZERO_WIDTH))
    for block in re.finditer(
        r"<tool_call>(.*?)</tool_call>", content, re.DOTALL
    ):
        inner = block.group(1)
        # 工具名在 <function=NAME> 里
        name_m = re.search(r"<function=([^\s>]+)>", inner)
        if not name_m:
            continue
        name = name_m.group(1).strip()
        # 参数: <parameter=KEY>...VALUE...</parameter>
        args: dict = {}
        for pm in re.finditer(
            r"<parameter=([^>]+)>(.*?)</parameter>",
            inner,
            re.DOTALL,
        ):
            key = pm.group(1).strip()
            val = pm.group(2).strip()
            args[key] = val
        calls.append((name, args))
    return calls


def _run_tool(name: str, args: dict, retries: int = 2) -> dict:
    """分发只读 Bangumi 工具；失败自动重试，仍失败则转 error 结果交由 LLM 回退。

    retries: 额外重试次数（默认 2，共最多 3 次尝试）。
    """
    last_err: Exception | None = None
    for _ in range(retries + 1):
        try:
            if name == "search_bangumi":
                return search_bangumi(**args)
            if name == "get_bangumi_detail":
                return get_bangumi_detail(**args)
            if name == "get_airing_now":
                return get_airing_now()
            return {"error": f"未知工具：{name}"}
        except Exception as e:  # noqa: BLE001
            last_err = e
            continue
    return {"error": f"工具 {name} 调用失败（重试 {retries} 次后仍失败）：{last_err}"}


@app.get("/health")
def health():
    return {
        "status": "ok",
        "corpus_ready": corpus is not None,
        "chunks": len(corpus.chunks) if corpus else 0,
    }


@app.post("/rebuild")
def rebuild():
    """重新构建 RAG 索引（规则/FAQ/番剧快照变更后调用，或重启服务）。"""
    global corpus
    try:
        corpus = build_corpus(_CORPUS_DIR, embed)
    except Exception as e:  # noqa: BLE001
        return {"status": "error", "detail": str(e)}
    return {"status": "rebuilt", "chunks": len(corpus.chunks) if corpus else 0}


@app.post("/chat")
async def chat(req: ChatRequest):
    """SSE 流式对话：RAG 检索 → 拼系统提示 → LLM（带 Bangumi 工具）→ 工具执行 → 流式回答。"""

    async def event_stream():
        global corpus  # event_stream 内部需要重新赋值 corpus,Python 闭包规则必须显式声明
        try:
            # 1) 检索相关分块（语料未就绪时先尝试懒加载一次）
            if corpus is None:
                try:
                    corpus = build_corpus(_CORPUS_DIR, embed)
                except Exception as e:  # noqa: BLE001
                    yield _sse(
                        {"type": "error", "content": f"语料未就绪（embedding 连接失败）：{e}"}
                    )
                    yield _sse({"type": "done", "content": ""})
                    return
            chunks = corpus.search(req.message, top_k=5)
            context = "\n\n".join(
                f"[来源:{c.source} / {c.title}]\n{c.text}" for c in chunks
            )
            messages = [
                {"role": "system", "content": SYSTEM_PROMPT.format(context=context)}
            ]
            # 1.5) 长对话上下文压缩：历史过长时只保留最近若干条，
            # 更早的以一条系统备注概括数量，避免逼近 token 上限导致报错。
            history = list(req.history)
            omitted = 0
            if len(history) > _MAX_HISTORY_MESSAGES:
                omitted = len(history) - _MAX_HISTORY_MESSAGES
                history = history[-_MAX_HISTORY_MESSAGES:]
                messages.append(
                    {
                        "role": "system",
                        "content": (
                            f"（已省略较早的 {omitted} 条历史消息，仅保留最近 "
                            f"{_MAX_HISTORY_MESSAGES} 条以维持上下文精简；"
                            "若需回顾更早内容，请用户主动提及。）"
                        ),
                    }
                )
            for h in history:
                messages.append({"role": h.role, "content": h.content})
            # 附件（V1 仅文本）：作为一条 user 消息注入，让 LLM 能引用文件内容
            att = req.attachment
            if att and att.content:
                # 安全上限：超长文本截断，避免逼近 token 上限导致报错
                MAX_ATTACH_CHARS = 30000
                content = att.content
                if len(content) > MAX_ATTACH_CHARS:
                    content = content[:MAX_ATTACH_CHARS] + "\n…（内容过长已截断）"
                messages.append(
                    {
                        "role": "user",
                        "content": (
                            f"【用户上传的文件《{att.filename or '未命名'}》内容如下，"
                            f"请基于它回答我的问题】\n{content}"
                        ),
                    }
                )
            messages.append({"role": "user", "content": req.message})

            # 2) 首轮：带 tools，非流式，检测 tool_calls。
            # 工具模式异常（模型不稳定等）时降级为无工具纯对话，让模型基于 RAG 直接答。
            try:
                first = chat_completion(
                    messages,
                    tools=TOOLS,
                    model=req.model,
                    temperature=req.temperature,
                )
            except Exception:
                first = chat_completion(
                    messages,
                    model=req.model,
                    temperature=req.temperature,
                )
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
                # 自定义 XML 工具调用兜底：部分模型（LongCat、Agnes AI 等）不走
                # OpenAI 标准 tool_calls,而是把工具调用写成 XML 标签塞进 content。
                # 按顺序尝试 LongCat 和 Agnes 两种格式,谁先匹配上就执行谁。
                xml_calls = _parse_longcat_tool_calls(assistant_msg.content)
                if not xml_calls:
                    xml_calls = _parse_agnes_tool_calls(assistant_msg.content)
                if xml_calls:
                    # 清空原始 XML 内容，避免它作为「回答」流到前端
                    yield _sse({"type": "tool_status", "content": "正在查询番剧库…"})
                    assistant_msg.content = ""
                    tool_blocks = []
                    for name, args in xml_calls:
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
            for token in chat_stream(
                messages,
                model=req.model,
                temperature=req.temperature,
            ):
                # 防御：跳过含 XML 的 token 继续流,避免中断整个回答
                if "longcat_tool_call" in token or "tool_call>" in token:/n                    continue
                yield _sse({"type": "token", "content": token})
        except RuntimeError as e:
            # 配置缺失（缺 API Key）等可预期错误：明确提示，便于排查
            yield _sse({"type": "error", "content": f"配置或依赖缺失：{e}"})
        except Exception as e:  # noqa: BLE001
            # 其余异常兜底，保证流正常结束
            yield _sse({"type": "error", "content": f"服务暂不可用：{e}"})
        yield _sse({"type": "done", "content": ""})

    return StreamingResponse(event_stream(), media_type="text/event-stream")
