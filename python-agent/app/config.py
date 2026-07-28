"""Agent 服务配置：从环境变量 / .env 读取，所有密钥不硬编码。"""
import os

from dotenv import load_dotenv

load_dotenv()


class Settings:
    # Chat 模型：LongCat（美团"龙猫"，OpenAI 兼容）。
    # 注：LongCat 无对外 embedding 接口，embedding 见下方"通义"配置（双供应商）。
    # base_url / 模型名以你在 LongCat API 开放平台申请的接入文档为准。
    llm_base_url_chat: str = os.getenv("LLM_BASE_URL_CHAT", "https://api.longcat.ai/v1")
    llm_api_key_chat: str = os.getenv("LLM_API_KEY_CHAT", "")
    llm_chat_model: str = os.getenv("LLM_CHAT_MODEL", "LongCat-Flash-Chat")

    # 通义千问 embedding（OpenAI 兼容 /compatible-mode）
    llm_base_url_embed: str = os.getenv(
        "LLM_BASE_URL_EMBED", "https://dashscope.aliyuncs.com/compatible-mode/v1"
    )
    llm_api_key_embed: str = os.getenv("LLM_API_KEY_EMBED", "")
    llm_embed_model: str = os.getenv("LLM_EMBED_MODEL", "text-embedding-v3")

    # Bangumi（只读 GET，仅需 User-Agent；与后端 bangumi.api.* 配置保持一致）
    bangumi_base_url: str = os.getenv("BANGUMI_BASE_URL", "https://bgmapi.anibt.net")
    bangumi_user_agent: str = os.getenv(
        "BANGUMI_USER_AGENT", "AcgSpace/1.0 (https://github.com/your-repo)"
    )

    # 服务
    port: int = int(os.getenv("PYTHON_AGENT_PORT", "8000"))


settings = Settings()
