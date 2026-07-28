"""LLM 客户端：OpenAI 兼容协议的 chat(stream) 与 embed。

- chat 用 DeepSeek（LLM_BASE_URL_CHAT / LLM_API_KEY_CHAT / LLM_CHAT_MODEL）。
- embed 用通义千问（LLM_BASE_URL_EMBED / LLM_API_KEY_EMBED / LLM_EMBED_MODEL）。
- 密钥仅来自 settings（.env / 环境变量），不硬编码。
- 客户端惰性创建：缺少密钥时在使用处抛清晰错误，避免 /health 在导入期崩溃。
"""
from openai import OpenAI

from app.config import settings


def _chat_client() -> OpenAI:
    if not settings.llm_api_key_chat:
        raise RuntimeError(
            "LLM_API_KEY_CHAT 未配置：请在 python-agent/.env 设置 DeepSeek API Key"
        )
    return OpenAI(base_url=settings.llm_base_url_chat, api_key=settings.llm_api_key_chat)


def _embed_client() -> OpenAI:
    if not settings.llm_api_key_embed:
        raise RuntimeError(
            "LLM_API_KEY_EMBED 未配置：请在 python-agent/.env 设置通义 embedding API Key"
        )
    return OpenAI(base_url=settings.llm_base_url_embed, api_key=settings.llm_api_key_embed)


def embed(texts: list[str]) -> list[list[float]]:
    """批量向量化，返回与输入等长的向量列表（保持顺序）。

    texts 为空时返回空列表，避免对空输入发起 API 调用。
    """
    if not texts:
        return []
    client = _embed_client()
    resp = client.embeddings.create(model=settings.llm_embed_model, input=texts)
    # 部分供应商不保证 data 顺序，按 index 归位
    ordered = [None] * len(resp.data)
    for item in resp.data:
        ordered[item.index] = item.embedding
    return ordered


def chat_stream(messages: list[dict], tools=None):
    """流式对话，yield 文本增量字符串（content token）。

    tools: OpenAI function-calling 工具 schema 列表（Phase 3 启用）。
    """
    client = _chat_client()
    kwargs = {
        "model": settings.llm_chat_model,
        "messages": messages,
        "stream": True,
        "temperature": 0.3,
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


def chat_completion(messages: list[dict], tools=None):
    """非流式对话，返回完整响应对象（用于检测 tool_calls）。

    当 LLM 返回 tool_calls 时，调用方执行工具并把结果回填 messages，再做流式最终回答。
    """
    client = _chat_client()
    kwargs = {
        "model": settings.llm_chat_model,
        "messages": messages,
        "stream": False,
        "temperature": 0.3,
    }
    if tools:
        kwargs["tools"] = tools
    return client.chat.completions.create(**kwargs)
