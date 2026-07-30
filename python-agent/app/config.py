"""Agent 服务配置：从环境变量 / .env 读取，所有密钥不硬编码。"""
import os

from dotenv import load_dotenv

load_dotenv()


class Settings:
    # Chat 模型：Agnes AI（免费，OpenAI 兼容，支持标准 tool_calls）。
    # 也可通过 LLM_BASE_URL_CHAT 环境变量切换回 LongCat 或其他供应商。
    llm_base_url_chat: str = os.getenv("LLM_BASE_URL_CHAT", "https://apihub.agnes-ai.com/v1")
    llm_api_key_chat: str = os.getenv("LLM_API_KEY_CHAT", "")
    llm_chat_model: str = os.getenv("LLM_CHAT_MODEL", "agnes-2.0-flash")

    # LongCat embedding（OpenAI 兼容，与 chat 共用同个 key）。
    # LongCat 的 /openai 路径也提供 text-embedding-ada-002 模型。
    llm_base_url_embed: str = os.getenv(
        "LLM_BASE_URL_EMBED", "https://api.longcat.chat/openai"
    )
    llm_api_key_embed: str = os.getenv("LLM_API_KEY_EMBED", "")
    llm_embed_model: str = os.getenv("LLM_EMBED_MODEL", "text-embedding-ada-002")

    # Bangumi（只读 GET，仅需 User-Agent；与后端 bangumi.api.* 配置保持一致）
    bangumi_base_url: str = os.getenv("BANGUMI_BASE_URL", "https://bgmapi.anibt.net")
    bangumi_user_agent: str = os.getenv(
        "BANGUMI_USER_AGENT", "AcgSpace/1.0 (https://github.com/your-repo)"
    )

    # 服务
    port: int = int(os.getenv("PYTHON_AGENT_PORT", "8000"))


settings = Settings()
