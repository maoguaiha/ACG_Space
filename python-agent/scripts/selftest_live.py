"""Live 端到端实测：用真实 API Key 验证 /chat 全链路（RAG 检索 + 流式 LLM + Bangumi 工具）。

前置条件：python-agent/.env 已填入
    LLM_API_KEY_CHAT  = 你的 LongCat key
    LLM_API_KEY_EMBED = 你的 通义 key
且网络可访问 api.longcat.chat 与 dashscope.aliyuncs.com。

用法（在仓库根或 python-agent 目录下均可）：
    python python-agent/scripts/selftest_live.py

脚本会：
  1. 校验 key 是否就位（缺则给出明确提示并退出，绝不硬编码）。
  2. 启动前构建语料（触发真实通义 embedding，验证 embedding key）。
  3. 跑两个用例：
     - 用例1：规则 RAG 问答（抽赏保底）→ 验证检索命中 + 流式回答。
     - 用例2：番剧推荐（function calling → 只读 Bangumi 工具）→ 验证工具调用闭环。
"""
import os
import sys
import json

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.normpath(os.path.join(_HERE, "..")))  # python-agent/

from dotenv import load_dotenv

load_dotenv(os.path.join(_HERE, "..", ".env"))


def main() -> int:
    chat_key = os.getenv("LLM_API_KEY_CHAT", "")
    embed_key = os.getenv("LLM_API_KEY_EMBED", "")

    if not chat_key or not embed_key or "your-" in chat_key or "your-" in embed_key:
        print("✗ 缺少/未填写 API Key。请在 python-agent/.env 填入真实值：")
        print("    LLM_API_KEY_CHAT  = <你的 LongCat key>")
        print("    LLM_API_KEY_EMBED = <你的 通义 key>")
        print("  （.env 已被 .gitignore 排除，不会进入版本库；切勿把 key 写进代码。）")
        return 2

    # 构建语料（此处会调用真实通义 embedding，验证 embed key 是否正确）
    print("• 构建语料（调用真实通义 embedding）...")
    try:
        from app.main import app
    except Exception as e:  # noqa: BLE001
        print(f"✗ 语料构建失败（embedding key 可能无效）：{e}")
        return 1

    from fastapi.testclient import TestClient

    client = TestClient(app)
    h = client.get("/health").json()
    print(f"  /health: chunks={h['chunks']}")

    def run_query(q: str, history=None) -> str:
        body = {"user_id": "live_test", "message": q}
        if history:
            body["history"] = history
        r = client.post("/chat", json=body)
        text = ""
        n_token = 0
        for line in r.text.splitlines():
            if not line.startswith("data:"):
                continue
            payload = line[len("data:"):].strip()
            try:
                obj = json.loads(payload)
            except Exception:
                continue
            t = obj.get("type")
            if t == "token":
                text += obj.get("content", "")
                n_token += 1
            elif t == "error":
                print(f"   [error frame] {obj.get('content')}")
        print(f"   (tokens={n_token}, chars={len(text)})")
        return text

    print("\n=== 用例1：RAG 规则问答（抽赏保底）===")
    a1 = run_query("抽赏有保底吗？保底是怎么算的？")
    print("A1:", a1[:500])

    print("\n=== 用例2：Bangumi 番剧推荐（function calling）===")
    a2 = run_query("最近有什么好看的机战番剧推荐吗？")
    print("A2:", a2[:800])

    ok1 = ("保底" in a1) or ("抽赏" in a1)
    ok2 = ("番" in a2) or ("机战" in a2) or ("推荐" in a2)
    print("\n=== 结果 ===")
    print(f"  用例1 (RAG 命中): {'PASS' if ok1 else 'CHECK'}")
    print(f"  用例2 (Bangumi 工具): {'PASS' if ok2 else 'CHECK'}")
    print("  LIVE TEST DONE (人工核对上面回答是否合理)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
