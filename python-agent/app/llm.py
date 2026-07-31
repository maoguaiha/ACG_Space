"""LLM 客户端：OpenAI 兼容 chat + 通义 text-embedding-v3 向量化。

- chat 由环境变量配置（LLM_BASE_URL_CHAT / LLM_API_KEY_CHAT / LLM_CHAT_MODEL）。
- embed 走通义千问 text-embedding-v3（DASHSCOPE_API_KEY）。
  曾用本地 FastEmbed(ONNX)，但模型需从 huggingface 在线下载，Railway 容器
  无缓存且 HF 不可达会卡死首轮对话，故换回通义 API（本地验证过的路径）。
- 密钥仅来自 settings（.env / 环境变量），不硬编码。
- 客户端惰性创建：在首次调用处初始化，避免导入期崩溃。
"""
import time

from openai import APIConnectionError, OpenAI

from app.config import settings


_embed_client = None


def _get_embed_client() -> OpenAI:
    """惰性初始化通义 embedding 客户端。"""
    global _embed_client
    if _embed_client is None:
        if not settings.dashscope_api_key:
            raise RuntimeError(
                "DASHSCOPE_API_KEY 未配置：请在 python-agent/.env 设置通义 embedding Key"
            )
        _embed_client = OpenAI(
            base_url=settings.dashscope_base_url,
            api_key=settings.dashscope_api_key,
        )
    return _embed_client


def _chat_client() -> OpenAI:
    if not settings.llm_api_key_chat:
        raise RuntimeError(
            "LLM_API_KEY_CHAT 未配置：请在 python-agent/.env 设置 Chat API Key"
        )
    return OpenAI(base_url=settings.llm_base_url_chat, api_key=settings.llm_api_key_chat)


def embed(texts: list[str]) -> list[list[float]]:
    """批量向量化（通义 text-embedding-v3），返回与输入等长的 float 向量列表。

    通义单次请求有批量上限（实测 ~10 条内安全），按 ≤8 分批规避。
    """
    if not texts:
        return []
    client = _get_embed_client()
    batch = 8
    vectors: list[list[float]] = []
    for i in range(0, len(texts), batch):
        chunk = texts[i : i + batch]
        resp = client.embeddings.create(
            model=settings.dashscope_embedding_model,
            input=chunk,
        )
        # 按输入顺序对齐（通义返回 data 顺序与输入一致）
        ordered = sorted(resp.data, key=lambda d: d.index)
        vectors.extend([v.embedding for v in ordered])
    return vectors


# 供应商切换时的模型名兼容映射：旧名 → 新名（None 表示用配置默认值）。
# 当 req.model 携带旧供应商模型名时,自动映射以避免 model_not_found 错误。
_MODEL_ALIASES = {
    "LongCat-2.0": None,
}


def _resolve_model(model: str | None) -> str | None:
    """解析模型名：别名映射 + 空值回退。"""
    if not model:
        return None
    return _MODEL_ALIASES.get(model, model)


def chat_stream(messages: list[dict], tools=None, model: str | None = None, temperature: float | None = None):
    """流式对话，yield 文本增量字符串（content token）。"""
    client = _chat_client()
    kwargs = {
        "model": _resolve_model(model) or settings.llm_chat_model,
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
        "model": _resolve_model(model) or settings.llm_chat_model,
        "messages": messages,
        "stream": False,
        "temperature": 0.3 if temperature is None else temperature,
    }
    if tools:
        kwargs["tools"] = tools
    return client.chat.completions.create(**kwargs)
