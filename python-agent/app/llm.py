"""LLM 客户端：OpenAI 兼容 chat + 本地 FastEmbed 向量化。

- chat 由环境变量配置（LLM_BASE_URL_CHAT / LLM_API_KEY_CHAT / LLM_CHAT_MODEL）。
- embed 本地运行（fastembed / ONNX），零网络依赖、无需 API Key。
- 密钥仅来自 settings（.env / 环境变量），不硬编码。
- 客户端/嵌入器惰性创建：在首次调用处初始化，避免导入期崩溃。
"""
import time

import numpy as np
from openai import APIConnectionError, OpenAI
from fastembed import TextEmbedding

from app.config import settings


_embedder = None


def _get_embedder() -> TextEmbedding:
    """惰性初始化本地嵌入器（首次调用时下载模型并缓存到 HuggingFace cache）。"""
    global _embedder
    if _embedder is None:
        # bge-small-zh-v1.5 约 33MB，中文语义向量，速度约 1000 条/秒 (CPU)
        _embedder = TextEmbedding(
            model_name="BAAI/bge-small-zh-v1.5",
            max_length=512,
        )
    return _embedder


def _chat_client() -> OpenAI:
    if not settings.llm_api_key_chat:
        raise RuntimeError(
            "LLM_API_KEY_CHAT 未配置：请在 python-agent/.env 设置 Chat API Key"
        )
    return OpenAI(base_url=settings.llm_base_url_chat, api_key=settings.llm_api_key_chat)


def embed(texts: list[str]) -> list[list[float]]:
    """本地批量向量化，返回与输入等长的 float 向量列表。"""
    if not texts:
        return []
    embedder = _get_embedder()
    # passage_embed 返回 Generator[np.ndarray]
    vectors = list(embedder.passage_embed(texts))
    return [v.tolist() for v in vectors]


def chat_stream(messages: list[dict], tools=None, model: str | None = None, temperature: float | None = None):
    """流式对话，yield 文本增量字符串（content token）。"""
    client = _chat_client()
    kwargs = {
        "model": model or settings.llm_chat_model,
        "messages": messages,
        "stream": True,
        "temperature": 0.3 if temperature is None else temperature,
    }
    if tools:
        kwargs["tools"] = tools
    stream = client.chat.completions.create(**kwargs)
    for chunk in stream:
        if not chunk.choices:
            continue
        delta = chunk.choices[0].delta
        if delta and delta.content:
            yield delta.content


def chat_completion(messages: list[dict], tools=None, model: str | None = None, temperature: float | None = None):
    """非流式对话，返回完整响应对象（用于检测 tool_calls）。"""
    client = _chat_client()
    kwargs = {
        "model": model or settings.llm_chat_model,
        "messages": messages,
        "stream": False,
        "temperature": 0.3 if temperature is None else temperature,
    }
    if tools:
        kwargs["tools"] = tools
    return client.chat.completions.create(**kwargs)
