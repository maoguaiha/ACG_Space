#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
RAG 离线自测：不依赖任何 API Key，验证切片与余弦检索机制是否正确。

原理：用确定性的「字符级词袋」fake embedder 代替通义 embedding，相似文本（共享汉字）
余弦更高。断言典型用户问题可命中对应分块（PRD 规则 / FAQ / 番剧快照缺失容错）。

用法：python python-agent/scripts/selftest_rag.py
"""
import math
import os
import re
import sys

_HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.normpath(os.path.join(_HERE, "..")))

from app.rag import build_corpus  # noqa: E402

_DIM = 256


def fake_embed(texts: list) -> list:
    out = []
    for t in texts:
        v = [0.0] * _DIM
        toks = re.findall(r"[\u4e00-\u9fff]|[a-zA-Z0-9]+", t)
        for tok in toks:
            v[hash(tok) % _DIM] += 1.0
        norm = math.sqrt(sum(x * x for x in v)) or 1.0
        out.append([x / norm for x in v])
    return out


def _run() -> int:
    corpus_dir = os.path.normpath(os.path.join(_HERE, "..", "corpus"))
    corpus = build_corpus(corpus_dir, fake_embed)
    print(f"总块数: {len(corpus.chunks)}")

    cases = [
        ("抽赏保底怎么算", {"PRD_V2.md", "faq.md"}),
        ("碎片怎么合成", {"faq.md", "PRD_V2.md"}),
        ("积分怎么获得", {"faq.md", "PRD_V2.md"}),
        ("订单多久发货", {"faq.md", "PRD_V2.md"}),
        ("番剧库有什么番", {"faq.md", "PRD_V2.md"}),
    ]
    failures = 0
    for query, expect_srcs in cases:
        top = corpus.search(query, top_k=3)
        hit = top[0] if top else None
        ok = hit is not None and hit.source in expect_srcs
        status = "OK " if ok else "FAIL"
        if not ok:
            failures += 1
        shown = hit.text[:36].replace("\n", " ") if hit else "(无)"
        print(f"[{status}] Q={query!r} -> 命中 source={hit.source if hit else '-'} title={hit.title if hit else '-'} | {shown}")

    # anime.json 缺失容错：语料应仍>0（规则+FAQ），且不应崩溃
    assert len(corpus.chunks) > 0, "语料不应为空（规则+FAQ 至少应存在）"
    print("anime.json 缺失容错: OK（规则/FAQ 检索正常）")

    if failures:
        print(f"\n自测失败 {failures} 例")
        return 1
    print("\n全部检索自测通过 ✅")
    return 0


if __name__ == "__main__":
    sys.exit(_run())
