"""
ACG Space python-agent 服务入口（FastAPI，无状态 AI 大脑）。

Phase 0：打通 SSE 管线（/chat 暂为 echo 流式桩），便于端到端联调。
Phase 2：在 /chat 中接入 RAG 检索 + 流式 LLM。
Phase 3：加入只读 Bangumi 工具（function calling）+ TTL 缓存。
"""
import asyncio
import json

from fastapi import FastAPI
from fastapi.responses import StreamingResponse

from app.config import settings
from app.schemas import ChatRequest

app = FastAPI(title="ACG Space Agent", version="0.1.0")


def _sse(obj: dict) -> str:
    """构造 SSE data 帧（与前端 useAgentApi 的解析约定一致：data:{json}\\n\\n）。"""
    return f"data:{json.dumps(obj, ensure_ascii=False)}\n\n"


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/rebuild")
def rebuild():
    # Phase 2 实现：从 corpus/ 重建 RAG 索引。
    return {"status": "rebuild scheduled (Phase 2)"}


@app.post("/chat")
async def chat(req: ChatRequest):
    """SSE 流式对话。Phase 0 为 echo 桩，后续替换为 RAG + LLM + Bangumi 工具。"""

    async def event_stream():
        yield _sse({"type": "token", "content": f"[agent online] 收到：{req.message}\n"})
        await asyncio.sleep(0.05)
        yield _sse({"type": "done", "content": ""})

    return StreamingResponse(event_stream(), media_type="text/event-stream")
