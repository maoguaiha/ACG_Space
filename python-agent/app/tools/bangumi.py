"""只读 Bangumi 工具：番剧搜索 / 详情 / 本周放送。

- 仅 GET + User-Agent 头，**无需 API Key**（复用后端 bangumi.api.* 配置）。
- 内存 TTL 缓存（1h）尊重 rate limit；工具失败由调用方回退 RAG。
- 解析按真实镜像 bgmapi.anibt.net 返回结构（已探测确认）。
"""
import json
import time

import httpx

from app.config import settings

_CACHE_TTL = 3600  # 1 小时
_cache: dict = {}

_client = httpx.Client(timeout=10.0)


def _get_json(path: str, params: dict = None) -> dict:
    """带 TTL 缓存的 GET JSON（仅 GET + User-Agent）。"""
    url = f"{settings.bangumi_base_url}{path}"
    key = url + (json.dumps(params, sort_keys=True) if params else "")
    now = time.time()
    if key in _cache:
        ts, data = _cache[key]
        if now - ts < _CACHE_TTL:
            return data
    resp = _client.get(
        url,
        params=params,
        headers={"User-Agent": settings.bangumi_user_agent, "Accept": "application/json"},
    )
    resp.raise_for_status()
    data = resp.json()
    _cache[key] = (now, data)
    return data


def _rating(d) -> float | None:
    r = d.get("rating") if isinstance(d, dict) else None
    if isinstance(r, dict):
        return r.get("score")
    return r


def search_bangumi(keyword: str, limit: int = 5) -> dict:
    """搜索番剧，返回前 limit 条（name/name_cn/summary/rating/url）。"""
    data = _get_json(f"/search/subject/{keyword}", {"type": 2, "responseGroup": "large"})
    items = data.get("list") or []
    out = []
    for it in items[:limit]:
        out.append(
            {
                "id": it.get("id"),
                "name": it.get("name"),
                "name_cn": it.get("name_cn") or "",
                "summary": (it.get("summary") or "")[:200],
                "rating": _rating(it),
                "url": it.get("url") or "",
            }
        )
    return {"count": len(out), "query": keyword, "items": out}


def get_bangumi_detail(bgm_id: int) -> dict:
    """番剧详情：名称 / 简介 / 评分 / 标签 / 集数。"""
    d = _get_json(f"/v0/subjects/{bgm_id}")
    return {
        "id": d.get("id"),
        "name": d.get("name"),
        "name_cn": d.get("name_cn") or "",
        "summary": (d.get("summary") or "")[:300],
        "rating": _rating(d),
        "tags": [t.get("name") for t in (d.get("tags") or [])][:10],
        "total_episodes": d.get("total_episodes"),
        "url": d.get("url") or f"https://bgm.tv/subject/{bgm_id}",
    }


def get_airing_now() -> dict:
    """本周放送列表（每天取前 3 部）。"""
    data = _get_json("/calendar")
    out = []
    for day in data[:7]:
        wd = (day.get("weekday") or {}).get("cn") or ""
        for it in (day.get("items") or [])[:3]:
            out.append(
                {
                    "weekday": wd,
                    "id": it.get("id"),
                    "name": it.get("name"),
                    "name_cn": it.get("name_cn") or "",
                    "rating": _rating(it),
                }
            )
    return {"count": len(out), "items": out}
