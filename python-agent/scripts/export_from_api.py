#!/usr/bin/env python3
"""
从后端公开 API 拉取番剧数据，生成 corpus/anime.json（免数据库连接）。
用法：
  python scripts/export_from_api.py                          # 前端公网代理
  python scripts/export_from_api.py https://<后端公网域名>    # 指定后端地址
"""
import json
import os
import sys
import urllib.request

STATUS_MAP = {0: "连载中", 1: "已完结", 2: "未开播"}

# 从后端字段 → export_anime.py 字段映射
FIELD_MAP = {
    "id": "id",
    "bgmId": "bgm_id",
    "title": "title",
    "titleOriginal": "title_original",
    "coverUrl": "cover_url",
    "summary": "summary",
    "totalEpisodes": "total_episodes",
    "publishYear": "publish_year",
    "status": "status",
    "rating": "rating",
    "genre": "genre",
    "featured": "featured",
}


def fetch_all(base_url):
    """不分页全量拉取（当前数据量小，后续可加分页逻辑）。"""
    # base_url 是前端公网地址时走 /api-proxy 代理，指定了后端地址时走原路径
    proxy_prefix = "/api-proxy" if "up.railway.app" in base_url else ""
    url = f"{base_url}{proxy_prefix}/api/anime/page?page=1&size=500"
    req = urllib.request.Request(url, headers={"Accept": "application/json"})
    with urllib.request.urlopen(req, timeout=15) as resp:
        body = json.loads(resp.read().decode())

    data = body.get("data", body)
    records = data.get("records", data.get("rows", data.get("list", body)))
    print(f"[fetch] 从 {url} 获取到 {len(records)} 条记录")
    return records


def transform(records):
    """驼峰 → 下划线 + 类型转换，与 export_anime.py 的 row_to_record 输出一致。"""
    result = []
    for r in records:
        rec = {}
        for src_key, dst_key in FIELD_MAP.items():
            val = r.get(src_key)
            # status 数字 → 中文
            if dst_key == "status" and val is not None:
                val = STATUS_MAP.get(int(val), "未知")
            # rating → float
            if dst_key == "rating" and val is not None:
                try:
                    val = float(val)
                except (ValueError, TypeError):
                    val = None
            # genre 逗号分隔字符串 → 列表
            if dst_key == "genre" and isinstance(val, str):
                val = [g.strip() for g in val.split(",") if g.strip()]
            # featured → bool
            if dst_key == "featured":
                val = bool(val)
            # summary 去 HTML 标签（简单处理）
            if dst_key == "summary" and val:
                val = (val.replace("<p>", "").replace("</p>", "\n")
                       .replace("<br>", "\n").replace("<br/>", "\n")
                       .replace("<b>", "").replace("</b>", "").strip())
            rec[dst_key] = val
        result.append(rec)
    return result


def main():
    if len(sys.argv) > 1:
        base = sys.argv[1].rstrip("/")
    elif os.getenv("BACKEND_URL"):
        base = os.getenv("BACKEND_URL").rstrip("/")
    else:
        # 默认：通过前端公网代理（用户可访问）
        base = "https://acgspace-production.up.railway.app"

    raw = fetch_all(base)
    records = transform(raw)

    here = os.path.dirname(os.path.abspath(__file__))
    out = os.path.normpath(os.path.join(here, "..", "corpus", "anime.json"))
    os.makedirs(os.path.dirname(out), exist_ok=True)
    with open(out, "w", encoding="utf-8") as f:
        json.dump(records, f, ensure_ascii=False, indent=2)
    print(f"EXPORTED {len(records)} anime records -> {out}")


if __name__ == "__main__":
    main()
