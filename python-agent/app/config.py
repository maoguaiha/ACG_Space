"""Agent 服务配置：从环境变量 / .env 读取，所有密钥不硬编码。"""
import os
import re

from dotenv import load_dotenv

load_dotenv()


def _clean(value: str | None, *, name: str) -> str:
    """清洗环境变量值：含换行 / 控制字符 / 异常长度 → 返回空串。

    Railway Variables UI 在粘贴 commit message 文本时容易误把整段话（含换行）
    粘成单条变量的值，例如 LLM_API_KEY_CHAT 的值变成
    "sk-...\\ncorrect (LLM_BASE_URL_CHAT=..., LLM_CHAT_MODEL=...)\\n..."
    — 这会直接导致 HTTP header 不合法（Bearer 后含换行）、OpenAI/urllib 抛
    "Invalid header value"。这里提前过滤，不让单条脏值拖垮整个服务。
    """
    if value is None:
        return ""
    if any(c in value for c in ("\n", "\r", "\0", "\t")):
        return ""
    if len(value) > 4096:
        return ""
    return value


class Settings:
    # Chat 模型：DeepSeek（深度求索，OpenAI 兼容，标准 tool_calls + 生成速度 ~4-6x LongCat）。
    # 曾用 LongCat-2.0：不支持标准 tool_calls（需 XML 兜底），且 streaming 生成仅 ~10 tokens/s，
    # 复杂番剧问答（232 tokens 回答）实测 streaming 23.7s。换 DeepSeek 后标准 tool_calls
    # 探测 0.8s + 生成速度 40-60 tokens/s，目标 30s -> 8-12s。
    llm_base_url_chat: str = _clean(os.getenv("LLM_BASE_URL_CHAT"), name="LLM_BASE_URL_CHAT") or "https://api.deepseek.com"
    llm_api_key_chat: str = _clean(os.getenv("LLM_API_KEY_CHAT"), name="LLM_API_KEY_CHAT")
    llm_chat_model: str = _clean(os.getenv("LLM_CHAT_MODEL"), name="LLM_CHAT_MODEL") or "deepseek-chat"

    # Embedding：通义千问 text-embedding-v3（阿里云 OpenAI 兼容端点）。
    # 曾用本地 FastEmbed(ONNX)，但模型需从 huggingface 在线下载，
    # Railway 容器无缓存且 HF 不可达时会卡死首轮对话，故换回通义 API。
    # 变量名向后兼容：优先 DASHSCOPE_*，回退旧的 LLM_*_EMBED（旧 .env / 脚本在用）。
    dashscope_base_url: str = _clean(
        os.getenv(
            "DASHSCOPE_BASE_URL",
            os.getenv("LLM_BASE_URL_EMBED", "https://dashscope.aliyuncs.com/compatible-mode/v1"),
        ),
        name="DASHSCOPE_BASE_URL",
    ) or "https://dashscope.aliyuncs.com/compatible-mode/v1"
    dashscope_api_key: str = _clean(
        os.getenv("DASHSCOPE_API_KEY", os.getenv("LLM_API_KEY_EMBED", "")),
        name="DASHSCOPE_API_KEY",
    )
    dashscope_embedding_model: str = _clean(
        os.getenv("DASHSCOPE_EMBEDDING_MODEL", os.getenv("LLM_EMBED_MODEL", "text-embedding-v3")),
        name="DASHSCOPE_EMBEDDING_MODEL",
    ) or "text-embedding-v3"

    # Bangumi（只读 GET，仅需 User-Agent；与后端 bangumi.api.* 配置保持一致）
    bangumi_base_url: str = _clean(os.getenv("BANGUMI_BASE_URL"), name="BANGUMI_BASE_URL") or "https://bgmapi.anibt.net"
    bangumi_user_agent: str = _clean(
        os.getenv("BANGUMI_USER_AGENT", "AcgSpace/1.0 (https://github.com/your-repo)"),
        name="BANGUMI_USER_AGENT",
    ) or "AcgSpace/1.0 (https://github.com/your-repo)"

    # 服务
    port: int = int(os.getenv("PYTHON_AGENT_PORT", "8000") or "8000")


settings = Settings()
