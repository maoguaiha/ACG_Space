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


def _image_url(d) -> str:
    """从 bangumi 响应中抽取封面图 URL（响应字段 images.common / large / medium）。"""
    if not isinstance(d, dict):
        return ""
    images = d.get("images") or {}
    if not isinstance(images, dict):
        return ""
    return images.get("common") or images.get("large") or images.get("medium") or ""


# 类型词同义扩展：Bangumi 搜索对单个类型词（如"悬疑"）返回的候选池很小且混入
# 欧美/国产番（实测"悬疑"9 条 0 日本、"热血"9 条 2 日本）。按类型词扩展搜索词
# 合并候选池后，日本番的数量显著提升（悬疑 -> 推理/惊悚 后 7 条、热血 -> 11 条）。
_SYNONYM_MAP = {
    "悬疑": ["悬疑", "推理", "惊悚"],
    "推理": ["推理", "悬疑"],
    "热血": ["热血", "战斗", "少年"],
    "战斗": ["战斗", "热血", "动作"],
    "机战": ["机战", "机器人", "萝卜"],
    "治愈": ["治愈", "日常", "温馨"],
    "恋爱": ["恋爱", "纯爱"],
    "搞笑": ["搞笑", "喜剧", "日常"],
    "奇幻": ["奇幻", "冒险", "异世界"],
    "科幻": ["科幻", "SF"],
}


def _expand_keywords(keyword: str) -> list[str]:
    """类型词同义扩展；具体番名/其他词不扩展（保持原搜索行为）。"""
    return _SYNONYM_MAP.get(keyword.strip(), [keyword.strip()])


def _is_jp_anime(bgm_id) -> bool:
    """判断条目是否为日本番剧（查 v0 详情接口的 meta_tags，TTL 缓存兜底）。"""
    if bgm_id is None:
        return False
    try:
        d = _get_json(f"/v0/subjects/{bgm_id}")
        tags = d.get("meta_tags") or []
        return "日本" in tags
    except Exception:
        # 详情查询失败时保守放行（不因过滤逻辑把整个搜索搞挂）
        return True


def search_bangumi(keyword: str, limit: int = 5) -> dict:
    """搜索番剧，返回前 limit 条**日本番剧**（name/name_cn/summary/rating/url/image）。

    搜索接口不返回国家信息且候选池小（~9 条）、混入欧美/国产番，故：
    1. 类型词先做同义扩展（如 悬疑 -> 悬疑/推理/惊悚），合并候选池；
    2. 并发查 v0 详情接口 meta_tags 过滤出含"日本"的条目；
    3. 截断到 limit。
    详情接口有 TTL 缓存，重复查询代价可忽略；并发控制在 8 内避免打爆镜像。
    """
    from concurrent.futures import ThreadPoolExecutor, as_completed

    keywords = _expand_keywords(keyword)
    # 并发发起各同义词搜索（镜像响应 0.6-2s 且不稳定，串行会放大到 3-6s）
    with ThreadPoolExecutor(max_workers=len(keywords)) as ex:
        search_results = list(ex.map(
            lambda kw: _get_json(f"/search/subject/{kw}", {"type": 2, "responseGroup": "large"}),
            keywords,
        ))
    pool = []
    seen = set()
    for data in search_results:
        for it in data.get("list") or []:
            iid = it.get("id")
            if iid is not None and iid not in seen:
                seen.add(iid)
                pool.append(it)

    # 候选相关性过滤：Bangumi 搜索对"番名"质量好（标题完全匹配），
    # 但对类型词模糊匹配会带一堆无关番剧（如搜"异度侵入"会带回哆啦A梦电影等）。
    # 只保留 title/name_cn/name 至少一个含原始 keyword（番名场景）或 keyword 前 2 字
    # （类型词场景如"热血"匹配"热血最强哥修罗"）。
    primary_kw = keyword.strip()
    kw_head = primary_kw[:2] if len(primary_kw) >= 2 else primary_kw
    filtered = []
    for it in pool:
        title_cn = (it.get("name_cn") or "").lower()
        title_raw = (it.get("name") or "").lower()
        title_alt = (it.get("title") or "").lower()
        haystack = title_cn + "|" + title_raw + "|" + title_alt
        if primary_kw.lower() in haystack or kw_head in haystack:
            filtered.append(it)
    pool = filtered

    # 并发过滤日本番：as_completed + 凑够 limit 提前停止，避免把整个候选池
    # 全查一遍（镜像详情接口每条约 0.5-1s，25 条全查 = 6-8s，只需查少量即可）。
    out = []
    pending = list(pool)
    with ThreadPoolExecutor(max_workers=8) as ex:
        futures = {ex.submit(_is_jp_anime, it.get("id")): it for it in pending}
        for fut in as_completed(futures):
            it = futures[fut]
            try:
                jp = fut.result()
            except Exception:
                jp = True  # 保守放行
            if jp:
                out.append(
                    {
                        "id": it.get("id"),
                        "name": it.get("name"),
                        "name_cn": it.get("name_cn") or "",
                        "summary": (it.get("summary") or "")[:200],
                        "rating": _rating(it),
                        "url": it.get("url") or "",
                        "image": _image_url(it),
                    }
                )
                if len(out) >= limit:
                    # 已凑够，取消剩余未执行的查询，提前退出
                    for f in futures:
                        if not f.done():
                            f.cancel()
                    break
    return {"count": len(out), "query": keyword, "items": out}
    return {"count": len(out), "query": keyword, "items": out}


def get_bangumi_detail(bgm_id: int) -> dict:
    """番剧详情：名称 / 简介 / 评分 / 标签 / 集数 / 封面 / 链接。"""
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
        "image": _image_url(d),
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
                    "image": _image_url(it),
                }
            )
    return {"count": len(out), "items": out}
