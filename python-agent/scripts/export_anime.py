#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
导出 biz_anime 表到 corpus/anime.json（供 RAG 检索「平台有哪些番 / 类似推荐」使用）。

特性：
- 只读 SELECT，不写业务库（守护「纯问答、不执行写操作」边界）。
- 幂等、可重跑：每次全量覆盖写入 anime.json。
- 连接信息从环境变量读取，默认值对齐 docker-compose.yml / application-dev.yml：
    DB_HOST=127.0.0.1  DB_PORT=13306  DB_NAME=acg_space
    DB_USER=root       DB_PWD=123456
- 字段对齐后端 BizAnime 实体；rating 转为 float、genre 拆为列表、status 转中文。
- 字段映射逻辑（row_to_record）为纯函数，可用 `python export_anime.py --selftest` 离线自测，无需真实数据库。

用法：
    python export_anime.py                 # 导出到默认路径 ../corpus/anime.json
    python export_anime.py /path/out.json  # 指定输出路径
    python export_anime.py --selftest      # 仅跑样例映射自测，不连库
"""
import os
import re
import sys
import json

# 导出字段（与后端 BizAnime 实体一一对应）
FIELDS = [
    "id", "bgm_id", "title", "title_original", "cover_url", "summary",
    "total_episodes", "publish_year", "status", "rating", "genre", "featured",
]

STATUS_MAP = {0: "连载中", 1: "已完结", 2: "未开播"}

_TAG_RE = re.compile(r"<[^>]+>")


def _strip_html(text: str) -> str:
    """去除摘要中的 HTML 标签，避免把 <p>/<br> 等噪声带进语料。"""
    if not text:
        return ""
    return _TAG_RE.sub("", text).strip()


def row_to_record(row: dict) -> dict:
    """将一行 biz_anime 映射为 RAG 用的干净记录（纯函数，便于离线单测）。"""
    rating = row.get("rating")
    try:
        rating_val = float(rating) if rating is not None else None
    except (TypeError, ValueError):
        rating_val = None

    genre_raw = row.get("genre") or ""
    genre_list = [g.strip() for g in genre_raw.split(",") if g.strip()]

    return {
        "id": str(row.get("id")) if row.get("id") is not None else None,
        "bgm_id": row.get("bgm_id"),
        "title": row.get("title") or "",
        "title_original": row.get("title_original") or "",
        "cover_url": row.get("cover_url") or "",
        "summary": _strip_html(row.get("summary")),
        "total_episodes": row.get("total_episodes"),
        "publish_year": row.get("publish_year"),
        "status": STATUS_MAP.get(row.get("status"), "未知"),
        "rating": rating_val,
        "genre": genre_list,
        "featured": bool(row.get("featured")),
    }


def get_connection():
    """建立只读 MySQL 连接（pymysql 惰性导入，便于无依赖时仅做 selftest）。"""
    import pymysql

    return pymysql.connect(
        host=os.getenv("DB_HOST", "127.0.0.1"),
        port=int(os.getenv("DB_PORT", "13306")),
        user=os.getenv("DB_USER", "root"),
        password=os.getenv("DB_PWD", "123456"),
        database=os.getenv("DB_NAME", "acg_space"),
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
        read_timeout=30,
        write_timeout=30,
    )


def export(out_path: str) -> list:
    """连库查询并写出 anime.json，返回记录列表。"""
    conn = get_connection()
    try:
        with conn.cursor() as cur:
            # 仅取未删除记录；del_flag 在历史数据可能为 NULL
            cur.execute(
                "SELECT {cols} FROM biz_anime WHERE del_flag = 0 OR del_flag IS NULL".format(
                    cols=", ".join(FIELDS)
                )
            )
            rows = cur.fetchall()
    finally:
        conn.close()

    records = [row_to_record(r) for r in rows]
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(records, f, ensure_ascii=False, indent=2)
    return records


def _selftest() -> None:
    """离线样例映射自测：覆盖含 HTML 摘要、未开播、空字段等边界。"""
    samples = [
        {
            "id": 1, "bgm_id": 123, "title": "测试番剧", "title_original": "Test Anime",
            "cover_url": "http://x/y.jpg", "summary": "  <p>这是<b>简介</b></p><br>  ",
            "total_episodes": 12, "publish_year": 2024, "status": 1,
            "rating": 8.5, "genre": "热血,异世界,治愈", "featured": 1,
        },
        {
            "id": 2, "bgm_id": None, "title": "未开播番", "title_original": "",
            "cover_url": None, "summary": None, "total_episodes": None,
            "publish_year": 2026, "status": 2, "rating": None, "genre": "", "featured": 0,
        },
    ]
    for s in samples:
        print(json.dumps(row_to_record(s), ensure_ascii=False))


def _default_out_path() -> str:
    here = os.path.dirname(os.path.abspath(__file__))
    return os.path.normpath(os.path.join(here, "..", "corpus", "anime.json"))


if __name__ == "__main__":
    if "--selftest" in sys.argv:
        _selftest()
        sys.exit(0)

    out_path = sys.argv[1] if len(sys.argv) > 1 else _default_out_path()
    records = export(out_path)
    print(f"EXPORTED {len(records)} anime records -> {out_path}")
