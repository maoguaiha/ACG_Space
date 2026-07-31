"""Standalone unit test for the streaming XML interceptor.

Strategy: extract the interceptor logic into a pure function and test it
directly, without importing the whole main.py (which needs FastAPI/OpenAI).

This mirrors the production code in main.py:355-386 exactly.
"""
import json
import sys
import types

ZWSP = ''
LONGCAT_OPEN = '<longcat_tool_call'
AGNES_OPEN = '<t' + ZWSP + 'ool_call>'


def stream_with_interceptor(messages, model, temperature, chat_stream):
    """Replicate the production interceptor block.

    Yields SSE frame strings of the form 'data:{json}\\n\\n'.

    Strategy:
      1. Buffer the stream instead of yielding eagerly.
      2. Hold each new token until the buffer is "safe" — i.e. it cannot
         start an XML opening tag for either provider.
      3. When the buffer is safe, flush it forward.
      4. If the buffer ever contains a full opening tag, yield an error
         frame and stop.
    This adds a tiny per-token latency (one token) but guarantees that no
    partial tag ever reaches the client.
    """
    def _sse(obj):
        return f'data:{json.dumps(obj, ensure_ascii=False)}\n\n'

    # Maximum length of an opening tag we care about. The longest opening
    # tag here is '<longcat_tool_call' (18 chars) so 32 is a safe upper bound.
    MAX_LOOKAHEAD = 32
    _buf = ''
    _xml_hit = False
    for token in chat_stream(messages, model=model, temperature=temperature):
        if _xml_hit:
            continue
        _buf += token
        # If the buffer contains an opening tag for either provider, error out.
        if LONGCAT_OPEN in _buf or AGNES_OPEN in _buf:
            _xml_hit = True
            yield _sse({'type': 'error', 'content': '模型响应异常（检测到工具调用 XML），请重试。'})
            return
        # Compute how much of the tail could still be the start of a tag.
        # If the buffer's tail contains '<', hold enough to confirm.
        safe_flush_until = max(0, len(_buf) - MAX_LOOKAHEAD)
        # Find the last '<' position in the tail (within MAX_LOOKAHEAD of end)
        last_lt = _buf.rfind('<', safe_flush_until)
        if last_lt < 0:
            # No '<' in the tail — everything is safe to flush
            yield _sse({'type': 'token', 'content': _buf})
            _buf = ''
        else:
            # Hold back from last_lt to confirm it's not an XML opening tag
            yield _sse({'type': 'token', 'content': _buf[:last_lt]})
            _buf = _buf[last_lt:]
            # Bound the buffer to avoid runaway memory
            if len(_buf) > MAX_LOOKAHEAD * 2:
                _buf = _buf[-MAX_LOOKAHEAD:]
    # Stream ended: flush any leftover content if not in error state
    if not _xml_hit and _buf:
        # If leftover looks like it could be a tag, drop it (defense)
        if LONGCAT_OPEN in _buf or AGNES_OPEN in _buf or _buf.lstrip().startswith('<'):
            yield _sse({'type': 'error', 'content': '模型响应异常（流末残留疑似 XML），请重试。'})
        else:
            yield _sse({'type': 'token', 'content': _buf})
    yield _sse({'type': 'done', 'content': ''})


def parse_frames(chunks):
    frames = []
    for c in chunks:
        if not c:
            continue
        for line in c.strip().splitlines():
            line = line.strip()
            if not line.startswith('data:'):
                continue
            payload = line[len('data:'):].strip()
            if payload:
                try:
                    frames.append(json.loads(payload))
                except Exception:
                    pass
    return frames


def case_clean():
    return [
        '## 推荐',
        '\n',
        '**《孤独摇滚》**（ぼっち・ざ・ろっく！）',
        '\n',
        '评分 9.0, 一部关于社恐女孩组乐队的青春故事。',
    ]


def case_longcat_split():
    """The exact bug pattern: tags are split mid-character across tokens."""
    return [
        '好的，我来帮你推荐。',
        '<',
        'longcat_',
        'tool_',
        'call>',
        'search_bangumi',
        '<',
        'longcat_arg_key>keyword</longcat_arg_key>',
        '<',
        'longcat_arg_value>推理</longcat_arg_value>',
        '</',
        'longcat_tool_',
        'call>',
    ]


def case_agnes_split():
    return [
        '让我搜一下。',
        '<',
        't' + ZWSP,
        'ool_',
        'call>',
        'search_bangumi',
        '<',
        'parameter=keyword>热血</parameter>',
        '</',
        't' + ZWSP,
        'ool_call>',
    ]


def run():
    print('=' * 60)
    print('Case 1: clean stream — every token must pass through')
    print('=' * 60)
    tokens = case_clean()
    chunks = list(stream_with_interceptor([], 'LongCat-2.0', 0.3, lambda *a, **k: iter(tokens)))
    frames = parse_frames(chunks)
    types = [f['type'] for f in frames]
    print(f'  frame types: {types}')
    assert types[-1] == 'done', f'expected done at end, got {types}'
    tokens_out = [f for f in frames if f['type'] == 'token']
    full = ''.join(t['content'] for t in tokens_out)
    assert full == ''.join(tokens), f'mismatch: {full!r}'
    print(f'  ✓ {len(tokens_out)} token frames, all preserved')
    print()

    print('=' * 60)
    print('Case 2: LongCat XML split (regression test for the reported bug)')
    print('=' * 60)
    tokens = case_longcat_split()
    chunks = list(stream_with_interceptor([], 'LongCat-2.0', 0.3, lambda *a, **k: iter(tokens)))
    frames = parse_frames(chunks)
    types = [f['type'] for f in frames]
    print(f'  frame types: {types}')
    # Must yield an error and stop (no further tokens)
    assert 'error' in types, f'expected error, got {types}'
    err_idx = types.index('error')
    # No token frame may contain raw XML
    for f in frames[:err_idx + 1]:
        if f['type'] == 'token':
            assert '<longcat_tool_call' not in (f['content'] or ''), f'leak: {f["content"]!r}'
            assert 'longcat_arg_key' not in (f['content'] or ''), f'leak: {f["content"]!r}'
    # No further frames after error
    assert all(f['type'] == 'error' for f in frames[err_idx:]), f'frames after error: {frames[err_idx:]}'
    print(f'  ✓ error frame: {frames[err_idx]["content"]!r}')
    print(f'  ✓ no XML leak into token frames')
    print(f'  ✓ stream stopped cleanly after error')
    print()

    print('=' * 60)
    print('Case 3: Agnes AI XML split (defense in depth for U+200B tag)')
    print('=' * 60)
    tokens = case_agnes_split()
    chunks = list(stream_with_interceptor([], 'LongCat-2.0', 0.3, lambda *a, **k: iter(tokens)))
    frames = parse_frames(chunks)
    types = [f['type'] for f in frames]
    print(f'  frame types: {types}')
    assert 'error' in types, f'expected error, got {types}'
    print(f'  ✓ error frame: {[f for f in frames if f["type"] == "error"][0]["content"]!r}')
    print()

    print('=' * 60)
    print('Case 4: clean prefix then LongCat XML — prefix must pass')
    print('=' * 60)
    tokens = ['好的，我来帮你推荐。', '<', 'longcat_', 'tool_', 'call>']
    chunks = list(stream_with_interceptor([], 'LongCat-2.0', 0.3, lambda *a, **k: iter(tokens)))
    frames = parse_frames(chunks)
    tokens_out = [f for f in frames if f['type'] == 'token']
    prefix = ''.join(t['content'] for t in tokens_out)
    print(f'  preserved prefix: {prefix!r}')
    assert prefix == '好的，我来帮你推荐。', f'unexpected prefix: {prefix!r}'
    assert any(f['type'] == 'error' for f in frames)
    print('  ✓ normal answer prefix preserved, then error fired')
    print()

    print('ALL 4 CASES PASSED ✓')


if __name__ == '__main__':
    run()
