#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Phase 3 离线自测：Bangumi 工具解析 + agent function-calling 循环，均无需 API Key / 真实网络。

1) 解析：monkeypatch bangumi._get_json 返回真实结构 fixture，断言三个工具字段抽取正确。
2) 循环：monkeypatch main.chat_completion（返回带 tool_calls 的消息）+ main.chat_stream（假回答），
   验证 /chat 先执行工具再流式输出。

用法：python python-agent/scripts/selftest_tools.py
"""
import os
import sys
import json
import re
import math

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.normpath(os.path.join(_HERE, "..")))

import app.tools.bangumi as bgm  # noqa: E402
import app.llm  # noqa: E402


def _fake_embed(texts):
    """确定性字符级词袋 embedder（离线自测用，避免依赖 embedding API Key）。"""
    out = []
    for t in texts:
        v = [0.0] * 256
        for tok in re.findall(r"[\u4e00-\u9fff]|[a-zA-Z0-9]+", t):
            v[hash(tok) % 256] += 1.0
        norm = math.sqrt(sum(x * x for x in v)) or 1.0
        out.append([x / norm for x in v])
    return out


# 必须在 import app.main 之前打桩 embed，否则模块加载时 build_corpus 会因缺 Key 崩溃
app.llm.embed = _fake_embed

# ---- 真实结构 fixture（探测 bgmapi.anibt.net 得到）----
_FIX = {
    "/search/subject/CLANNAD": {
        "list": [
            {
                "id": 123,
                "name": "CLANNAD",
                "name_cn": "CLANNAD",
                "summary": "<p>京都动画催泪番</p>",
                "rating": {"score": 8.7},
                "url": "https://bgm.tv/subject/123",
            }
        ]
    },
    "/v0/subjects/123": {
        "id": 123,
        "name": "CLANNAD",
        "name_cn": "CLANNAD",
        "summary": "京都动画出品的催泪番",
        "tags": [{"name": "催泪"}, {"name": "京都"}],
        "rating": {"score": 8.7},
        "total_episodes": 23,
    },
    "/calendar": [
        {
            "weekday": {"cn": "星期一"},
            "items": [
                {
                    "id": 456,
                    "name": "x",
                    "name_cn": "某番",
                    "rating": {"score": 7.0},
                }
            ],
        }
    ],
}


def _fake_get(path: str, params=None):
    if path not in _FIX:
        raise AssertionError(f"unexpected path {path}")
    return _FIX[path]


def test_parsing() -> None:
    bgm._get_json = _fake_get
    r = bgm.search_bangumi("CLANNAD")
    assert r["items"][0]["name"] == "CLANNAD"
    assert r["items"][0]["rating"] == 8.7
    assert "京都动画" in r["items"][0]["summary"]  # HTML 未清洗但字段取得到
    d = bgm.get_bangumi_detail(123)
    assert d["tags"] == ["催泪", "京都"]
    assert d["total_episodes"] == 23
    c = bgm.get_airing_now()
    assert c["items"][0]["weekday"] == "星期一"
    print("[OK] Bangumi 工具解析（search/detail/calendar）字段正确")


class _Fn:
    def __init__(self, name, arguments):
        self.name = name
        self.arguments = arguments


class _TC:
    def __init__(self, tid, name, arguments):
        self.id = tid
        self.function = _Fn(name, arguments)

    def model_dump(self):
        return {
            "id": self.id,
            "type": "function",
            "function": {"name": self.function.name, "arguments": self.function.arguments},
        }


class _Msg:
    def __init__(self, content, tool_calls):
        self.content = content
        self.tool_calls = tool_calls


class _Choice:
    def __init__(self, message):
        self.message = message


class _Resp:
    def __init__(self, message):
        self.choices = [_Choice(message)]


def test_agent_loop() -> None:
    import app.main as main_mod  # noqa: E402
    import app.llm  # noqa: E402

    flag = {"tool_called": False}

    def fake_search(keyword, limit=5):
        flag["tool_called"] = True
        return {
            "count": 1,
            "query": keyword,
            "items": [
                {"id": 123, "name": "CLANNAD", "name_cn": "CLANNAD",
                 "summary": "催泪番", "rating": 8.7, "url": "x"}
            ],
        }

    def fake_chat_completion(messages, tools=None):
        # 当问题像推荐类，返回带 tool_calls 的 assistant 消息
        last = messages[-1]["content"]
        if "类似" in last or "推荐" in last:
            return _Resp(_Msg("", [_TC("call_1", "search_bangumi", '{"keyword":"CLANNAD"}')]))
        return _Resp(_Msg("", None))

    def fake_chat_stream(messages, tools=None):
        # 断言工具结果已回填进 messages
        joined = json.dumps(messages, ensure_ascii=False)
        assert "tool" in joined and "CLANNAD" in joined, "工具结果未回填"
        yield "（基于 Bangumi）为你推荐《CLANNAD》。"

    main_mod.chat_completion = fake_chat_completion
    main_mod.chat_stream = fake_chat_stream
    main_mod.search_bangumi = fake_search

    from fastapi.testclient import TestClient  # noqa: E402

    client = TestClient(main_mod.app)
    r = client.post(
        "/chat",
        json={"user_id": "u1", "message": "类似CLANNAD的番推荐", "history": []},
    )
    assert r.status_code == 200, f"/chat 状态码异常: {r.status_code}"
    body = r.text
    assert '"type": "token"' in body, "未收到 token 帧"
    assert '"type": "done"' in body, "未收到 done 帧"
    assert flag["tool_called"] is True, "工具未被调用"
    print("[OK] agent 工具循环：先执行 Bangumi 工具 → 回填 → 流式回答")


def _run() -> int:
    test_parsing()
    test_agent_loop()
    print("\nPhase 3 离线自测全部通过 ✅")
    return 0


if __name__ == "__main__":
    sys.exit(_run())
