"""Live smoke test for the agent latency-fix build (commit ae529b7+da78ae1).

Covers the four chains that were touched:
  1. embed() — DashScope text-embedding-v3 (replaces FastEmbed deadlock)
  2. chat_stream() — LongCat streaming without tools (skip-probe path)
  3. _needs_tools() — intent heuristic (skip probe for non-anime)
  4. Full /chat SSE — end-to-end through FastAPI TestClient

Requires real keys in python-agent/.env (LLM_API_KEY_CHAT + DASHSCOPE_API_KEY).
Run with the project's Python that has fastapi/openai installed:
  python python-agent/scripts/selftest_live2.py
"""
import io
import json
import os
import sys
import time

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))

from app.config import settings  # noqa: E402
from app.llm import embed, chat_stream, chat_completion  # noqa: E402


def _fmt(ok, msg):
    print(f'  [{"✓" if ok else "✗ FAIL"}] {msg}')
    return ok


def test_embed():
    print('\n=== 1. embed() DashScope text-embedding-v3 ===')
    if not settings.dashscope_api_key:
        return _fmt(False, f'DASHSCOPE_API_KEY missing (read={settings.dashscope_api_key!r})')
    t0 = time.time()
    vecs = embed(["积分怎么攒？", "推荐机战番"])
    dt = time.time() - t0
    ok = len(vecs) == 2 and len(vecs[0]) == 1024 and all(isinstance(x, float) for x in vecs[0])
    return _fmt(ok, f'embed(2 texts) -> {len(vecs)} vectors, dim={len(vecs[0]) if vecs else 0}, {dt:.2f}s')


def test_chat_stream():
    print('\n=== 2. chat_stream() LongCat no-tools (simple question) ===')
    if not settings.llm_api_key_chat:
        return _fmt(False, f'LLM_API_KEY_CHAT missing (read={settings.llm_api_key_chat!r})')
    messages = [
        {"role": "system", "content": "你是 ACG Space 官方 AI 助手。回答简洁。不输出 XML。"},
        {"role": "user", "content": "积分怎么攒？"},
    ]
    t0 = time.time()
    tokens = []
    try:
        for tok in chat_stream(messages, model=None, temperature=0.3):
            tokens.append(tok)
    except Exception as e:
        return _fmt(False, f'chat_stream raised: {e}')
    dt = time.time() - t0
    text = "".join(tokens)
    ok = len(text) > 10 and "longcat_tool_call" not in text
    print(f'    first-token {dt:.2f}s, total {len(text)} chars')
    print(f'    snippet: {text[:80]!r}')
    return _fmt(ok, f'streamed {len(text)} chars in {dt:.2f}s, no XML leak')


def test_needs_tools():
    print('\n=== 3. _needs_tools() intent heuristic ===')
    from app.main import _needs_tools  # noqa: E402
    cases = [
        ("积分怎么攒？", False),
        ("兑换规则是什么", False),
        ("我的订单物流到哪了", False),
        ("推荐几部机战番", True),
        ("类似《孤独摇滚》的番", True),
        ("搜一下《葬送的芙莉莲》", True),
    ]
    all_ok = True
    for q, want in cases:
        got = _needs_tools(q)
        ok = got == want
        all_ok &= ok
        print(f'    [{"✓" if ok else "✗"}] {q!r} -> needs_tools={got} (want {want})')
    return _fmt(all_ok, f'{len(cases)} samples')


def test_full_chat():
    print('\n=== 4. Full /chat SSE (TestClient) ===')
    try:
        from fastapi.testclient import TestClient
        from app.main import app
    except Exception as e:
        return _fmt(False, f'import TestClient failed: {e}')

    # Simple question (skip-probe path)
    client = TestClient(app)
    t0 = time.time()
    payload = {
        "user_id": "selftest",
        "message": "你好",
        "history": [],
        "model": "LongCat-2.0",
        "temperature": 0.3,
    }
    try:
        with client.stream("POST", "/chat", json=payload) as resp:
            print(f'    HTTP {resp.status_code}')
            frames = []
            for line in resp.iter_lines():
                if line and line.startswith("data:"):
                    try:
                        frames.append(json.loads(line[5:].strip()))
                    except Exception:
                        pass
    except Exception as e:
        return _fmt(False, f'/chat raised: {e}')
    dt = time.time() - t0
    types = [f.get("type") for f in frames]
    print(f'    frame types: {types}')
    print(f'    total {dt:.2f}s')
    tokens_out = [f.get("content", "") for f in frames if f.get("type") == "token"]
    text = "".join(tokens_out)
    errs = [f for f in frames if f.get("type") == "error"]
    ok = bool(text) and not errs and "longcat_tool_call" not in text and "<tool_call>" not in text
    print(f'    reply: {text[:80]!r}')
    return _fmt(ok, f'/chat ok in {dt:.2f}s, {len(tokens_out)} token frames, 0 errors')


def main():
    print(f'config: chat={settings.llm_chat_model} embed_model={settings.dashscope_embedding_model}')
    print(f'  chat key set: {bool(settings.llm_api_key_chat)}')
    print(f'  dashscope key set: {bool(settings.dashscope_api_key)}')
    results = [
        test_embed(),
        test_chat_stream(),
        test_needs_tools(),
        test_full_chat(),
    ]
    print('\n' + "=" * 50)
    passed = sum(1 for r in results if r)
    print(f'LIVE SMOKE: {passed}/{len(results)} passed')
    sys.exit(0 if passed == len(results) else 1)


if __name__ == "__main__":
    main()
