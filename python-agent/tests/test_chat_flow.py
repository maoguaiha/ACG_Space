"""L1 module-level unit tests for the chat flow (mock, no network, no keys).

Loads main.py via importlib (the 'python-agent' dir has a hyphen so it is
NOT importable as a normal package). Replaces module-level functions
(chat_stream / chat_completion / embed / _run_tool) with fakes, then drives
the /chat endpoint's event_stream coroutine directly and inspects the SSE
frames — without ever touching the network.

Scenarios covered:
  1. non-anime question  -> probe (chat_completion) is SKIPPED, single stream
  2. anime question      -> probe runs (tool path)
  3. XML tool call in probe content -> tool executed, result fed back
  4. corpus unavailable  -> degrades to pure-LLM (still answers, no error)
  5. history > limit     -> only the latest N messages are forwarded
"""
import asyncio
import importlib.util
import io
import json
import sys
import types

ROOT = r'C:\Users\Administrator\Desktop\材料\学习\ai\study\java\ACG_Space'
AGENT_DIR = ROOT + r'\python-agent'
MAIN_PATH = AGENT_DIR + r'\app\main.py'

# Make 'app' importable (main.py does `from app.config import settings`).
if AGENT_DIR not in sys.path:
    sys.path.insert(0, AGENT_DIR)

_spec = importlib.util.spec_from_file_location("agent_main_l1", MAIN_PATH)
_m = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_m)


class FakeEmbed:
    """Returns a fixed 1024-dim vector per text (DashScope text-embedding-v3 shape)."""
    def __init__(self):
        self.calls = []
    def __call__(self, texts):
        self.calls.append(list(texts))
        return [[0.01 * i for i in range(1024)] for _ in texts]


class FakeChatStream:
    """Yields fixed tokens; records the messages it received."""
    def __init__(self, tokens=None):
        self.tokens = tokens or ["好的，", "这是", "测试回答。"]
        self.calls = []
    def __call__(self, messages, model=None, temperature=None):
        self.calls.append(messages)
        for t in self.tokens:
            yield t


class FakeCompletion:
    """Returns a canned assistant message; records the messages."""
    def __init__(self, content="", tool_calls=None):
        self.content = content
        self.tool_calls = tool_calls
        self.calls = []
    def __call__(self, messages, **kwargs):
        self.calls.append((messages, kwargs))
        msg = types.SimpleNamespace(content=self.content, tool_calls=self.tool_calls)
        return types.SimpleNamespace(choices=[types.SimpleNamespace(message=msg)])


class FakeCorpus:
    """In-memory corpus stub; search returns fixed chunks."""
    def __init__(self, chunks=None):
        self.chunks = chunks or []
        self._search_calls = 0
    def search(self, query, top_k=3):
        self._search_calls += 1
        return self.chunks[:top_k]


def make_request(message, history=None):
    """Build a ChatRequest-like object (schemas.ChatRequest)."""
    hist = []
    for h in (history or []):
        hist.append({"role": h.get("role", "user"), "content": h.get("content", "")})
    return types.SimpleNamespace(
        user_id="test",
        conversation_id=None,
        message=message,
        history=[types.SimpleNamespace(role=h["role"], content=h["content"]) for h in hist],
        model="LongCat-2.0",
        temperature=0.3,
        attachment=None,
    )


def drain(req, module):
    """Drive the /chat event_stream and return parsed SSE frames."""
    async def _run():
        resp = await module.chat(req)
        chunks = []
        async for c in resp.body_iterator:
            chunks.append(c)
        return chunks

    raw = asyncio.run(_run())
    frames = []
    for c in raw:
        if not c:
            continue
        if isinstance(c, bytes):
            c = c.decode("utf-8")
        for line in c.strip().splitlines():
            line = line.strip()
            if line.startswith("data:"):
                payload = line[5:].strip()
                if payload:
                    frames.append(json.loads(payload))
    return frames


def frames_text(frames):
    return "".join(f.get("content", "") for f in frames if f.get("type") == "token")


# ---------------------------------------------------------------------------
# Test 1: non-anime -> probe skipped, single stream
# ---------------------------------------------------------------------------
def test_skip_probe_for_non_anime():
    fake_stream = FakeChatStream()
    fake_comp = FakeCompletion()
    old_s, old_c, old_e, old_r = _m.chat_stream, _m.chat_completion, _m.embed, _m._run_tool
    _m.chat_stream = fake_stream
    _m.chat_completion = fake_comp
    _m.embed = FakeEmbed()
    _m._run_tool = lambda name, args: {"ok": True}
    _m.corpus = FakeCorpus()
    try:
        frames = drain(make_request("积分怎么攒？"), _m)
    finally:
        _m.chat_stream, _m.chat_completion, _m.embed, _m._run_tool = old_s, old_c, old_e, old_r

    assert len(fake_comp.calls) == 0, f"probe should be SKIPPED, got {len(fake_comp.calls)} calls"
    assert len(fake_stream.calls) == 1, f"stream should run exactly once, got {len(fake_stream.calls)}"
    text = frames_text(frames)
    assert "好的，" in text, f"expected streamed text, got {text!r}"
    assert not any(f.get("type") == "error" for f in frames), "unexpected error frame"
    print("  ✓ skip-probe: chat_completion=0, chat_stream=1, text ok")


# ---------------------------------------------------------------------------
# Test 2: anime question -> probe runs (tool path taken)
# ---------------------------------------------------------------------------
def test_anime_runs_probe():
    fake_stream = FakeChatStream()
    fake_comp = FakeCompletion(content="")
    old_s, old_c, old_e = _m.chat_stream, _m.chat_completion, _m.embed
    _m.chat_stream = fake_stream
    _m.chat_completion = fake_comp
    _m.embed = FakeEmbed()
    _m.corpus = FakeCorpus()
    try:
        frames = drain(make_request("推荐几部机战番"), _m)
    finally:
        _m.chat_stream, _m.chat_completion, _m.embed = old_s, old_c, old_e

    assert len(fake_comp.calls) == 1, f"anime question should run probe, got {len(fake_comp.calls)} calls"
    # tools=TOOLS must be passed to the probe
    assert fake_comp.calls[0][1].get("tools") is not None, "probe must carry tools schema"
    print("  ✓ anime probe: chat_completion=1 with tools, then streamed")


# ---------------------------------------------------------------------------
# Test 3: XML tool call in probe content -> tool executed, result fed back
# ---------------------------------------------------------------------------
def test_xml_tool_call_in_probe():
    xml_content = (
        "<longcat_tool_call>search_bangumi\n"
        "<longcat_arg_key>keyword</longcat_arg_key>\n"
        "<longcat_arg_value>机战</longcat_arg_value>\n"
        "</longcat_tool_call>"
    )
    fake_stream = FakeChatStream()
    fake_comp = FakeCompletion(content=xml_content)
    tool_results = {}
    old_s, old_c, old_e = _m.chat_stream, _m.chat_completion, _m.embed

    def fake_run_tool(name, args):
        tool_results["name"] = name
        tool_results["args"] = args
        return {"title": "某机战番", "image": "http://x/cover.jpg"}

    _m.chat_stream = fake_stream
    _m.chat_completion = fake_comp
    _m.embed = FakeEmbed()
    _m._run_tool = fake_run_tool
    _m.corpus = FakeCorpus()
    try:
        frames = drain(make_request("推荐机战番"), _m)
    finally:
        _m.chat_stream, _m.chat_completion, _m.embed = old_s, old_c, old_e

    assert tool_results.get("name") == "search_bangumi", f"tool not executed, got {tool_results}"
    assert tool_results.get("args", {}).get("keyword") == "机战", f"wrong args, got {tool_results}"
    # The tool result must be fed back into the messages for the stream call
    stream_msgs = fake_stream.calls[0] if fake_stream.calls else []
    joined = json.dumps(stream_msgs, ensure_ascii=False)
    assert "某机战番" in joined, f"tool result not fed back: {joined[:200]!r}"
    # No XML must leak to the client
    text = frames_text(frames)
    assert "longcat_tool_call" not in text, "XML leaked to client"
    print("  ✓ xml-tool: search_bangumi executed, result fed back, no XML leak")


# ---------------------------------------------------------------------------
# Test 4: corpus unavailable -> pure-LLM fallback (still answers)
# ---------------------------------------------------------------------------
def test_corpus_down_fallback():
    fake_stream = FakeChatStream()
    fake_comp = FakeCompletion()
    old_s, old_c, old_e, old_corpus = _m.chat_stream, _m.chat_completion, _m.embed, _m.corpus
    _m.chat_stream = fake_stream
    _m.chat_completion = fake_comp
    _m.embed = FakeEmbed()
    _m.corpus = None

    def broken_build(*a, **k):
        raise RuntimeError("embedding API down")
    _m.build_corpus = broken_build

    try:
        frames = drain(make_request("你好"), _m)
    finally:
        _m.chat_stream, _m.chat_completion, _m.embed, _m.corpus = old_s, old_c, old_e, old_corpus

    text = frames_text(frames)
    assert "好的，" in text, f"expected pure-LLM answer despite corpus down, got {text!r}"
    assert not any(f.get("type") == "error" for f in frames), "must NOT error when corpus down"
    print("  ✓ corpus-down: degraded to pure LLM, still answered, no error")


# ---------------------------------------------------------------------------
# Test 5: history > limit -> only latest N forwarded
# ---------------------------------------------------------------------------
def test_history_truncation():
    fake_stream = FakeChatStream()
    fake_comp = FakeCompletion()
    old_s, old_c, old_e = _m.chat_stream, _m.chat_completion, _m.embed
    _m.chat_stream = fake_stream
    _m.chat_completion = fake_comp
    _m.embed = FakeEmbed()
    _m.corpus = FakeCorpus()

    # 6 history messages; _MAX_HISTORY_MESSAGES = 4
    history = [{"role": "user" if i % 2 == 0 else "assistant", "content": f"msg{i}"} for i in range(6)]
    try:
        frames = drain(make_request("再问一句", history), _m)
    finally:
        _m.chat_stream, _m.chat_completion, _m.embed = old_s, old_c, old_e

    stream_msgs = fake_stream.calls[0] if fake_stream.calls else []
    # Count how many history-origin messages are present (roles user/assistant, not system)
    hist_msgs = [m for m in stream_msgs if m.get("role") in ("user", "assistant")]
    # 4 history messages + 1 current question = 5 user/assistant messages max
    assert len(hist_msgs) <= 5, f"history not truncated: {len(hist_msgs)} messages forwarded"
    # The latest message must be present (msg5), the oldest (msg0) must be gone
    contents = json.dumps(stream_msgs, ensure_ascii=False)
    assert "msg5" in contents, "latest message missing after truncation"
    assert "msg0" not in contents, "oldest message should have been truncated away"
    # Only 4 of the 6 history messages may survive (msg2..msg5)
    survived = [f"msg{i}" for i in range(6) if f"msg{i}" in contents]
    assert len(survived) == 4, f"expected exactly 4 history messages to survive, got {survived}"
    print(f"  ✓ history-trunc: {len(hist_msgs)} user/assistant msgs (4 hist + 1 current), oldest dropped, latest kept")


# ---------------------------------------------------------------------------
# Test 6: streaming interceptor soft fallback — when LongCat emits XML in
# the streaming phase, the interceptor must NOT yield an error frame. If
# the probe already executed tools, it must yield their results as tokens.
# ---------------------------------------------------------------------------
def test_streaming_soft_fallback():
    fake_stream = FakeChatStream()
    xml_content = (
        "<longcat_tool_call>search_bangumi\n"
        "<longcat_arg_key>keyword</longcat_arg_key>\n"
        "<longcat_arg_value>机战</longcat_arg_value>\n"
        "</longcat_tool_call>"
    )
    fake_comp = FakeCompletion(content=xml_content)
    old_s, old_c, old_e, old_r = _m.chat_stream, _m.chat_completion, _m.embed, _m._run_tool

    def fake_run_tool(name, args):
        return {"title": "某机战番A", "rating": 8.5, "year": 2020}

    _m.chat_stream = fake_stream
    _m.chat_completion = fake_comp
    _m.embed = FakeEmbed()
    _m._run_tool = fake_run_tool
    _m.corpus = FakeCorpus()
    try:
        # Ask an anime question so the probe runs. But monkey-patch chat_stream
        # to emit XML in the streaming phase, simulating LongCat re-emitting.
        xml_tokens = ['好的，让我再', '<', 'longcat_', 'tool_', 'call>']
        _m.chat_stream = lambda messages, **kw: iter(xml_tokens)
        frames = drain(make_request("推荐机战番"), _m)
    finally:
        _m.chat_stream, _m.chat_completion, _m.embed, _m._run_tool = old_s, old_c, old_e, old_r

    errs = [f for f in frames if f.get("type") == "error"]
    assert not errs, f"soft-fallback should NOT yield error frames, got {errs}"
    # Tool result must appear as a token (or at least the notice must)
    types = [f.get("type") for f in frames]
    assert "token" in types, f"expected token frames with tool results, got {types}"
    full = "".join(f.get("content", "") for f in frames if f.get("type") == "token")
    assert "某机战番A" in full, f"tool result not surfaced in fallback: {full!r}"
    assert "longcat_tool_call" not in full, f"XML leaked despite fallback: {full!r}"
    print("  ✓ streaming-soft-fallback: no error, tool result surfaced, no XML leak")


def run():
    print("=== L1: chat-flow mock tests ===")
    test_skip_probe_for_non_anime()
    test_anime_runs_probe()
    test_xml_tool_call_in_probe()
    test_corpus_down_fallback()
    test_history_truncation()
    test_streaming_soft_fallback()
    print("\nL1 ALL 6 TESTS PASSED ✓")


if __name__ == "__main__":
    run()
