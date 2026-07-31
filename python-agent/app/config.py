"""Agent 服务配置：从环境变量 / .env 读取，所有密钥不硬编码。"""
import os

from dotenv import load_dotenv

load_dotenv()


class Settings:
    # Chat 模型：LongCat（美团"龙猫"，OpenAI 兼容，中文工具调用效果好）。
    llm_base_url_chat: str = os.getenv("LLM_BASE_URL_CHAT", "https://api.longcat.chat/openai")
    llm_api_key_chat: str = os.getenv("LLM_API_KEY_CHAT", "")
    llm_chat_model: str = os.getenv("LLM_CHAT_MODEL", "LongCat-2.0")

    # Embedding：通义千问 text-embedding-v3（阿里云 OpenAI 兼容端点）。
    # 曾用本地 FastEmbed(ONNX)，但模型需从 huggingface 在线下载，
    # Railway 容器无缓存且 HF 不可达时会卡死首轮对话，故换回通义 API。
    dashscope_base_url: str = os.getenv(
        "DASHSCOPE_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1"
    )
    dashscope_api_key: str = os.getenv("DASHSCOPE_API_KEY", "")
    dashscope_embedding_model: str = os.getenv(
        "DASHSCOPE_EMBEDDING_MODEL", "text-embedding-v3"
    )

    # Bangumi（只读 GET，仅需 User-Agent；与后端 bangumi.api.* 配置保持一致）
    bangumi_base_url: str = os.getenv("BANGUMI_BASE_URL", "https://bgmapi.anibt.net")
    bangumi_user_agent: str = os.getenv(
        "BANGUMI_USER_AGENT", "AcgSpace/1.0 (https://github.com/your-repo)"
    )

    # 服务
    port: int = int(os.getenv("PYTHON_AGENT_PORT", "8000"))


settings = Settings()
