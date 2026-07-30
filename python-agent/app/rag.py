"""RAG 检索：加载语料 → 切片 → 向量化 → 内存余弦检索。

设计：
- 语料来源：corpus/rules/*.md（PRD 等规则文档）、corpus/faq.md（高频问答）、corpus/anime.json（番剧快照）。
- 切片：按 `## ` 标题切分小节；超长小节再按段落/长度二次切分（≈400 字）。
- 向量化：通过注入的 embed_fn（生产用 llm.embed，依赖通义 API）；便于离线自测时替换为 fake embedder。
- 检索：内存线性扫描余弦相似度，语料仅数百段，无需向量库。
- anime.json 缺失时容错：仅告警并跳过番剧快照源，不影响规则/FAQ 检索。
"""
import json
import math
import os
import re
from dataclasses import dataclass, field

_CHUNK_MAX = 400
_HEADING_RE = re.compile(r"(?m)^##\s+")
_PARAGRAPH_RE = re.compile(r"\n+")


@dataclass
class Chunk:
    text: str
    source: str  # 文件名或 "anime"
    title: str
    meta: dict = field(default_factory=dict)


def _split_long(text: str, maxlen: int = _CHUNK_MAX) -> list[str]:
    """超长文本按段落二次切分，尽量保留语义完整。"""
    if not text.strip():
        return []
    if len(text) <= maxlen:
        return [text.strip()]
    paras = [p.strip() for p in _PARAGRAPH_RE.split(text) if p.strip()]
    out: list[str] = []
    cur = ""
    for para in paras:
        if len(cur) + len(para) + 1 <= maxlen:
            cur = (cur + "\n" + para).strip()
        else:
            if cur:
                out.append(cur)
            cur = para
    if cur:
        out.append(cur)
    return out


def _split_markdown(md_text: str, source: str, default_title: str) -> list[Chunk]:
    """按 `## ` 标题切分；首段（无标题）用 default_title；超长小节二次切分。"""
    parts = _HEADING_RE.split(md_text)
    chunks: list[Chunk] = []
    if parts and parts[0].strip():
        for sub in _split_long(parts[0]):
            chunks.append(Chunk(sub, source, default_title))
    for part in parts[1:]:
        lines = part.split("\n", 1)
        title = lines[0].strip()
        body = lines[1].strip() if len(lines) > 1 else ""
        for sub in _split_long(body):
            chunks.append(Chunk(sub, source, title))
    return chunks


def _load_markdown_dir(corpus_dir: str) -> list[Chunk]:
    chunks: list[Chunk] = []
    rules_dir = os.path.join(corpus_dir, "rules")
    md_files = []
    if os.path.isdir(rules_dir):
        md_files += [
            (os.path.join(rules_dir, f), f)
            for f in os.listdir(rules_dir)
            if f.endswith(".md")
        ]
    faq_path = os.path.join(corpus_dir, "faq.md")
    if os.path.isfile(faq_path):
        md_files.append((faq_path, "faq.md"))
    for path, name in md_files:
        with open(path, "r", encoding="utf-8") as fh:
            text = fh.read()
        default_title = os.path.splitext(name)[0]
        chunks.extend(_split_markdown(text, name, default_title))
    return chunks


def _load_anime(corpus_dir: str) -> list[Chunk]:
    path = os.path.join(corpus_dir, "anime.json")
    if not os.path.isfile(path):
        # 容错：番剧快照由 export_anime.py 连库生成，缺失时不阻断规则/FAQ 检索
        print(f"[rag] 警告：未找到 {path}，跳过番剧快照源（运行 scripts/export_anime.py 生成）")
        return []
    with open(path, "r", encoding="utf-8") as fh:
        records = json.load(fh)
    chunks: list[Chunk] = []
    for rec in records:
        title = rec.get("title") or rec.get("title_original") or "未知番剧"
        genres = "、".join(rec.get("genre", [])) if rec.get("genre") else ""
        summary = rec.get("summary") or ""
        text = f"番剧《{title}》（{rec.get('status', '')}）类型：{genres}。简介：{summary}"
        chunks.append(
            Chunk(
                text=text,
                source="anime",
                title=title,
                meta={
                    "id": rec.get("id"),
                    "bgm_id": rec.get("bgm_id"),
                    "rating": rec.get("rating"),
                },
            )
        )
    return chunks


def _cosine(a: list[float], b: list[float]) -> float:
    if not a or not b:
        return 0.0
    dot = sum(x * y for x, y in zip(a, b))
    na = math.sqrt(sum(x * x for x in a))
    nb = math.sqrt(sum(x * y for x, y in zip(b, b)))
    if na == 0 or nb == 0:
        return 0.0
    return dot / (na * nb)


class Corpus:
    """内存语料库：切片 + 向量 + 余弦检索。"""

    def __init__(self, embed_fn):
        self.embed_fn = embed_fn
        self.chunks: list[Chunk] = []
        self.vectors: list[list[float]] = []

    def build(self, corpus_dir: str) -> int:
        chunks = _load_markdown_dir(corpus_dir) + _load_anime(corpus_dir)
        # 批量向量化（embed_fn 内部已处理空列表）
        texts = [c.text for c in chunks]
        vectors = self.embed_fn(texts)
        self.chunks = chunks
        self.vectors = vectors
        return len(chunks)

    def search(self, query: str, top_k: int = 5) -> list[Chunk]:
        if not self.chunks:
            return []
        qv = self.embed_fn([query])[0]
        scored = [
            (_cosine(qv, v), i) for i, v in enumerate(self.vectors)
        ]
        scored.sort(key=lambda x: x[0], reverse=True)
        return [self.chunks[i] for _, i in scored[:top_k]]


def build_corpus(corpus_dir: str, embed_fn) -> Corpus:
    """构建语料库（供 main.py 启动时调用）。"""
    corpus = Corpus(embed_fn)
    n = corpus.build(corpus_dir)
    print(f"[rag] 已构建语料：{n} 个分块，来源目录 {corpus_dir}")
    return corpus
