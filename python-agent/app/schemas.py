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
