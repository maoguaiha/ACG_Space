"""agent 启动时从后端 API 拉取番剧数据生成 anime.json。"""
import json
import os
import sys

try:
    from urllib.request import Request, urlopen
except ImportError:
    from urllib2 import Request, urlopen


_CORPUS_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "corpus")

STATUS_MAP = {0: "连载中", 1: "已完结", 2: "未开播"}

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


def fetch_anime_json(backend_url: str) -> list[dict]:
    """从后端 API 不分页全量拉取番剧记录。"""
    url = f"{backend_url}/api/anime/page?page=1&size=500"
    req = Request(url, headers={"Accept": "application/json"})
    with urlopen(req, timeout=15) as resp:
        body = json.loads(resp.read().decode())
    data = body.get("data", body)
    records = data.get("records", data.get("rows", data.get("list", body)))
    print(f"[anime-loader] 从 {url} 获取到 {len(records)} 条记录")
    return records


def transform(records: list) -> list[dict]:
    """驼峰→下划线，与 export_anime.py 的 row_to_record 输出一致。"""
    result = []
    for r in records:
        rec = {}
        for sk, dk in FIELD_MAP.items():
            val = r.get(sk)
            if dk == "status" and val is not None:
                val = STATUS_MAP.get(int(val), "未知")
            if dk == "rating" and val is not None:
                try:
                    val = float(val)
                except (ValueError, TypeError):
                    val = None
            if dk == "genre" and isinstance(val, str):
                val = [g.strip() for g in val.split(",") if g.strip()]
            if dk == "featured":
                val = bool(val)
            if dk == "summary" and val:
                val = (val.replace("<p>", "").replace("</p>", "\n")
                       .replace("<br>", "\n").replace("<br/>", "\n")
                       .replace("<b>", "").replace("</b>", "").strip())
            rec[dk] = val
        result.append(rec)
    return result


def ensure_anime_json(backend_url: str) -> str:
    """确保 corpus/anime.json 存在，不存在则从后端 API 拉取生成。"""
    out_path = os.path.join(_CORPUS_DIR, "anime.json")
    if os.path.isfile(out_path):
        print(f"[anime-loader] {out_path} 已存在，跳过拉取")
        return out_path
    try:
        raw = fetch_anime_json(backend_url)
        records = transform(raw)
    except Exception as e:
        print(f"[anime-loader] 从后端拉取失败 ({e})，跳过番剧语料")
        return None

    os.makedirs(_CORPUS_DIR, exist_ok=True)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(records, f, ensure_ascii=False, indent=2)
    print(f"[anime-loader] 已生成 {out_path}，共 {len(records)} 条番剧")
    return out_path


if __name__ == "__main__":
    # 命令行直接运行时使用参数或 BACKEND_URL 环境变量
    url = sys.argv[1] if len(sys.argv) > 1 else os.getenv("BACKEND_URL", "http://localhost:18083")
    ensure_anime_json(url)
