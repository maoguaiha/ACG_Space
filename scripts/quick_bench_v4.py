#!/usr/bin/env python3
"""快速对比压测: HikariCP=30 vs 旧数据 HikariCP=10"""

import concurrent.futures, json, statistics, sys, time, urllib.request

BASE = "http://localhost:8080"
TIMEOUT = 30
REQUESTS = 200

# 只测公开接口（新的 SecurityConfig 下仍可匿名访问）
EPS = {
    "anime_list": "/api/anime/list?page=1&size=5",
    "featured":   "/api/anime/featured",
}

def http_get(url):
    start = time.perf_counter()
    try:
        req = urllib.request.Request(url, method="GET")
        with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
            body = resp.read()
            lat = (time.perf_counter() - start) * 1000
            ok = False
            try: ok = json.loads(body).get("code") == 200
            except: pass
            return {"ok": ok, "latency_ms": lat}
    except Exception as e:
        lat = (time.perf_counter() - start) * 1000
        return {"ok": False, "latency_ms": lat, "error": type(e).__name__}

def bench(name, path, workers):
    url = f"{BASE}{path}"
    wall_start = time.perf_counter()
    results = []
    with concurrent.futures.ThreadPoolExecutor(max_workers=workers) as pool:
        for f in concurrent.futures.as_completed([pool.submit(http_get, url) for _ in range(REQUESTS)]):
            results.append(f.result())
    wall_time = time.perf_counter() - wall_start
    
    ok = [r for r in results if r["ok"]]
    lat = sorted([r["latency_ms"] for r in results])
    def pct(d, p):
        if not d: return 0
        k = (len(d)-1)*p/100; f = int(k); c = k-f
        return d[f]*(1-c)+d[f+1]*c if f+1<len(d) else d[f]
    
    return {
        "name": name, "conc": workers, "ok": len(ok), "fail": len(results)-len(ok),
        "qps": len(results)/wall_time if wall_time else 0,
        "avg": statistics.mean(lat) if lat else 0,
        "p50": pct(lat,50), "p90": pct(lat,90), "p95": pct(lat,95), "p99": pct(lat,99),
        "wall_s": wall_time,
    }

print("""
╔══════════════════════════════════════════════════════════════╗
║  HikariCP=30 vs 旧数据(10) 对比压测                          ║
╚══════════════════════════════════════════════════════════════╝
""")

# 预热
for _ in range(5):
    for _, path in EPS.items():
        http_get(f"{BASE}{path}")
time.sleep(1)

# HikariCP=30 新数据
print("═══ HikariCP=30 (新) ═══")
new_results = []
for level in [1, 5, 10, 20, 30, 50, 80, 100]:
    for name, path in EPS.items():
        r = bench(name, path, level)
        new_results.append(r)
        flag = "✅" if r["fail"] == 0 else "❌"
        print(f"  {flag} C={level:>3d} {name:<12s} QPS={r['qps']:>6.0f} avg={r['avg']:>5.1f}ms P99={r['p99']:>5.1f}ms ok={r['ok']}")
    time.sleep(0.5)

# 对比旧数据
OLD_DATA = {
    "anime_list": {1: 87, 5: 449, 10: 630, 20: 628, 30: 679, 50: 702, 80: 690, 100: 667},
    "featured":   {1: 134, 5: 800, 10: 1380, 20: 1808, 30: 1814, 50: 1607, 80: 1684, 100: 1603},
}

print("\n═══ 对比: HikariCP=30 vs 10 ═══")
print(f"{'接口':<12s} {'C':>4s} {'旧QPS':>7s} {'新QPS':>7s} {'提升':>7s}")
for name in ["anime_list", "featured"]:
    for level in [1, 5, 10, 20, 30, 50, 80, 100]:
        old_qps = OLD_DATA[name].get(level, 0)
        new_r = [r for r in new_results if r["name"] == name and r["conc"] == level]
        new_qps = new_r[0]["qps"] if new_r else 0
        pct_gain = ((new_qps / old_qps - 1) * 100) if old_qps else 0
        flag = "🚀" if pct_gain > 20 else ("📈" if pct_gain > 5 else ("➡️" if pct_gain > -5 else "📉"))
        print(f"{name:<12s} {level:>4d} {old_qps:>7.0f} {new_qps:>7.0f} {flag} {pct_gain:>+5.0f}%")

# 保存结果
with open("bench_v4_result.json", "w") as f:
    json.dump(new_results, f, indent=2, ensure_ascii=False)
print("\n📁 数据已保存")
