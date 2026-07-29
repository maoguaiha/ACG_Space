"""Agent 服务请求 / 响应模型（Pydantic）。"""
from typing import List, Optional

from pydantic import BaseModel


class ChatMessage(BaseModel):
    role: str  # user / assistant / system
    content: str


class ChatRequest(BaseModel):
    user_id: str
    conversation_id: Optional[str] = None
    message: str
    history: List[ChatMessage] = []
    model: Optional[str] = None  # 覆盖默认 LLM_CHAT_MODEL（前端「AI 设置」透传）
    temperature: Optional[float] = None  # 覆盖默认采样温度（0~1）
