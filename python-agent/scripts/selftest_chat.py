#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
/chat 整条 SSE 管线离线集成自测（无需 API Key）。

做法：在导入 app.main 之前，把 app.llm.embed / app.llm.chat_stream 替换为桩，
从而验证「检索 → 拼接系统提示(含上下文) → 流式 SSE 返回」全链路无误。

用法：python python-agent/scripts/selftest_chat.py
"""
import math
import os
import re
import sys

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.normpath(os.path.join(_HERE, "..")))

import app.llm  # noqa: E402

_DIM = 256


def _fake_embed(texts):
    out = []
    for t in texts:
        v = [0.0] * _DIM
        for tok in re.findall(r"[\u4e00-\u9fff]|[a-zA-Z0-9]+", t):
            v[hash(tok) % _DIM] += 1.0
        norm = math.sqrt(sum(x * x for x in v)) or 1.0
        out.append([x / norm for x in v])
    return out


def _fake_chat_stream(messages):
    # 验证系统提示确实注入了检索上下文
    system = messages[0]["content"]
    assert "[来源:" in system, "系统提示未注入检索上下文"
    yield "【测试流式回复】上下文已注入，问题已收到。"
    yield " 这是第二段 token。"


# 打桩后再导入 main（main 启动时用 app.llm.embed 构建语料）
app.llm.embed = _fake_embed
app.llm.chat_stream = _fake_chat_stream
from app.main import app  # noqa: E402
from fastapi.testclient import TestClient  # noqa: E402


def _run() -> int:
    client = TestClient(app)
    # /health
    h = client.get("/health")
    assert h.status_code == 200 and h.json()["status"] == "ok"
    print("GET /health ->", h.json())

    # /chat SSE
    r = client.post(
        "/chat",
        json={"user_id": "u1", "message": "抽赏保底怎么算", "history": []},
    )
    assert r.status_code == 200, f"/chat 状态码异常: {r.status_code}"
    body = r.text
    print("--- /chat SSE 响应(前 300 字) ---")
    print(body[:300])
    assert '"type": "token"' in body, "未收到 token 帧"
    assert '"type": "done"' in body, "未收到 done 帧"
    print("\n/chat SSE 管线自测通过 ✅（检索+上下文注入+流式返回 全链路正常）")
    return 0


if __name__ == "__main__":
    sys.exit(_run())
