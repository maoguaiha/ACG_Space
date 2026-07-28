"""
ACG Space python-agent 服务入口（FastAPI，无状态 AI 大脑）。

Phase 0：打通 SSE 管线（/chat 暂为 echo 流式桩）。
Phase 2：/chat 接入 RAG 检索 + 流式 LLM（DeepSeek chat / 通义 embedding）。
Phase 3：加入只读 Bangumi 工具（function calling）+ TTL 缓存。
"""
import asyncio
import json
import os

from fastapi import FastAPI
from fastapi.responses import StreamingResponse

from app.config import settings
from app.llm import chat_stream, embed
from app.prompts import SYSTEM_PROMPT
from app.rag import build_corpus
from app.schemas import ChatRequest

app = FastAPI(title="ACG Space Agent", version="0.2.0")

_BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
_CORPUS_DIR = os.path.join(_BASE_DIR, "corpus")

# 启动时构建语料（向量化）。缺 embedding Key 时此处会抛错，由 uvicorn 启动失败暴露，便于及时发现配置缺失。
corpus = build_corpus(_CORPUS_DIR, embed)


def _sse(obj: dict) -> str:
    """构造 SSE data 帧（与前端 useAgentApi 的解析约定一致：data:{json}\\n\\n）。"""
    return f"data:{json.dumps(obj, ensure_ascii=False)}\n\n"


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
    """SSE 流式对话：RAG 检索 → 拼系统提示 → 流式 LLM 生成。"""

    async def event_stream():
        try:
            # 1) 检索相关分块
            chunks = corpus.search(req.message, top_k=5)
            context = "\n\n".join(
                f"[来源:{c.source} / {c.title}]\n{c.text}" for c in chunks
            )
            # 2) 组装 messages
            messages = [
                {"role": "system", "content": SYSTEM_PROMPT.format(context=context)}
            ]
            for h in req.history:
                messages.append({"role": h.role, "content": h.content})
            messages.append({"role": "user", "content": req.message})
            # 3) 流式生成
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
