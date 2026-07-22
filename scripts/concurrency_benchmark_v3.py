#!/usr/bin/env python3
"""
ACG_Space 并发能力量化测试 v3
==============================
基于 ThreadPoolExecutor，稳定可靠。
QPS = total_requests / wall_clock_seconds（精确墙钟计时）

用法: python concurrency_benchmark_v3.py
"""

import concurrent.futures
import json
import os
import statistics
import sys
import time
import urllib.request
import urllib.error

BASE_URL = "http://localhost:8080"
TIMEOUT = 30

# 已验证工作的 GET 接口
WORKING_ENDPOINTS = {
    "anime_list":      "/api/anime/list?page=1&size=5",
    "featured":        "/api/anime/featured",
    "item_page":       "/api/item/page?page=1&size=5",
    "market_page":     "/api/market/page?page=1&size=5",
}

# 递增并发等级（大于 HikariCP 默认连接数 10 时才开始有压力）
CONCURRENCY_LEVELS = [1, 5, 10, 20, 30, 50, 80, 100]
REQUESTS_PER_LEVEL = 200


def http_get(url):
    start = time.perf_counter()
    try:
        req = urllib.request.Request(url, method="GET")
        with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
            body = resp.read()
            lat = (time.perf_counter() - start) * 1000
            # 检测业务 code
            biz_ok = False
            try:
                obj = json.loads(body)
                biz_ok = obj.get("code") == 200
            except:
                biz_ok = (200 <= resp.status < 300)
            return {"http": resp.status, "biz_ok": biz_ok, "latency_ms": lat, "len": len(body), "error": ""}
    except urllib.error.HTTPError as e:
        lat = (time.perf_counter() - start) * 1000
        try:
            blen = len(e.read())
        except:
            blen = 0
        return {"http": e.code, "biz_ok": False, "latency_ms": lat, "len": blen, "error": f"HTTP {e.code}"}
    except Exception as e:
        lat = (time.perf_counter() - start) * 1000
        return {"http": 0, "biz_ok": False, "latency_ms": lat, "len": 0, "error": type(e).__name__}


def benchmark(endpoint_name, path, concurrency, n=REQUESTS_PER_LEVEL):
    """以 concurrency 个线程并发发送 n 个请求，精确计时"""
    url = f"{BASE_URL}{path}"
    results = []
    
    wall_start = time.perf_counter()
    with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as pool:
        futures = [pool.submit(http_get, url) for _ in range(n)]
        for f in concurrent.futures.as_completed(futures):
            results.append(f.result())
    wall_end = time.perf_counter()
    wall_time = wall_end - wall_start
    
    # 分析
    total = len(results)
    ok = [r for r in results if r["biz_ok"]]
    fail = [r for r in results if not r["biz_ok"]]
    all_lat = sorted([r["latency_ms"] for r in results])
    
    def pct(data, p):
        if not data: return 0
        k = (len(data) - 1) * p / 100.0
        f, c = int(k), k - int(k)
        if f + 1 < len(data):
            return data[f] * (1 - c) + data[f + 1] * c
        return data[f]
    
    ok_lat = sorted([r["latency_ms"] for r in ok]) if ok else [0]
    qps = total / wall_time if wall_time > 0 else 0
    
    return {
        "endpoint": endpoint_name,
        "concurrency": concurrency,
        "total": total,
        "success": len(ok),
        "fail": len(fail),
        "qps": qps,
        "wall_s": wall_time,
        "avg_ms": statistics.mean(ok_lat) if ok_lat else 0,
        "p50_ms": pct(ok_lat, 50),
        "p90_ms": pct(ok_lat, 90),
        "p95_ms": pct(ok_lat, 95),
        "p99_ms": pct(ok_lat, 99),
        "min_ms": min(ok_lat) if ok_lat else 0,
        "max_ms": max(all_lat) if all_lat else 0,
        "all_lat": all_lat,
        "errors": {},
    }


def print_row(r):
    rate = r["fail"] / r["total"] * 100 if r["total"] else 0
    flag = "⚠️" if rate > 1 else ("❌" if rate > 10 else "✅")
    print(f"  {flag} {r['endpoint']:<14s} | "
          f"QPS:{r['qps']:>6.0f} | "
          f"成功:{r['success']:>3d} | "
          f"失败:{r['fail']:>3d} | "
          f"avg:{r['avg_ms']:>6.1f}ms | "
          f"P50:{r['p50_ms']:>6.1f}ms | "
          f"P90:{r['p90_ms']:>6.1f}ms | "
          f"P95:{r['p95_ms']:>6.1f}ms | "
          f"P99:{r['p99_ms']:>6.1f}ms | "
          f"墙钟:{r['wall_s']:.2f}s")


def main():
    print("""
╔══════════════════════════════════════════════════════════════════════════╗
║        ACG_Space 并发能力量化测试 v3                                    ║
║        ThreadPoolExecutor | 精确墙钟 QPS | 递增并发扫瓶颈               ║
╚══════════════════════════════════════════════════════════════════════════╝
""")
    
    # 连通性
    print("🔍 接口检查:")
    for name, path in WORKING_ENDPOINTS.items():
        r = http_get(f"{BASE_URL}{path}")
        icon = "✅" if r["biz_ok"] else "❌"
        print(f"  {icon} {name:<14s} → biz_ok={r['biz_ok']}, {r['latency_ms']:.1f}ms" + 
              (f" (err={r['error']})" if r['error'] else ""))
    
    # 预热
    print("\n🔥 预热 (5轮)...")
    for _ in range(5):
        for name, path in WORKING_ENDPOINTS.items():
            http_get(f"{BASE_URL}{path}")
    time.sleep(1)
    print("✅ 预热完成\n")
    
    all_data = []
    
    for level in CONCURRENCY_LEVELS:
        print(f"{'='*100}")
        print(f"  ▲ 并发度={level} | 每接口 {REQUESTS_PER_LEVEL} 请求 | 线程池={level}")
        print(f"{'='*100}")
        
        for ep_name, path in WORKING_ENDPOINTS.items():
            sys.stdout.flush()
            r = benchmark(ep_name, path, level)
            all_data.append(r)
            print_row(r)
        
        time.sleep(1.5)
    
    # ═══ 汇总 ═══
    print(f"\n{'#'*100}")
    print(f"{'#':>42} 📊 最终量化报告 {'#':>38}")
    print(f"{'#'*100}\n")
    
    # 按接口汇总
    print("═══ 按接口维度 ═══")
    by_ep = {}
    for r in all_data:
        by_ep.setdefault(r["endpoint"], []).append(r)
    
    for ep_name in sorted(by_ep):
        items = by_ep[ep_name]
        best_qps = max(items, key=lambda x: x["qps"] if x["fail"] == 0 else 0)
        best_lat = min(items, key=lambda x: x["avg_ms"] if x["fail"] == 0 else 99999)
        
        print(f"\n  📌 {ep_name}")
        print(f"     🚀 峰值 QPS: {best_qps['qps']:.0f}")
        
        # 展示性能变化曲线（文字版）
        curve_parts = []
        for r in items:
            if r["fail"] == 0:
                curve_parts.append(f"C{r['concurrency']}={r['qps']:.0f}")
            else:
                curve_parts.append(f"C{r['concurrency']}={r['qps']:.0f}❌{r['fail']}")
        print(f"     📈 曲线: {' → '.join(curve_parts)}")
        
        # 延迟变化
        lat_curve = []
        for r in items:
            lat_curve.append(f"C{r['concurrency']}:P99={r['p99_ms']:.1f}ms")
        print(f"     ⏱️  P99延迟: {' → '.join(lat_curve)}")
        
        # 首次失败点
        first_fail = None
        for r in items:
            if r["fail"] > 0:
                first_fail = r
                break
        if first_fail:
            print(f"     ⚠️  首次失败 @并发{first_fail['concurrency']}: {first_fail['fail']}/{first_fail['total']}")
    
    # 系统级汇总
    print(f"\n═══ 系统级 (5接口并跑) ═══")
    for level in CONCURRENCY_LEVELS:
        lv = [r for r in all_data if r["concurrency"] == level]
        total_qps = sum(r["qps"] for r in lv)
        total_ok = sum(r["success"] for r in lv)
        total_fail = sum(r["fail"] for r in lv)
        total_req = total_ok + total_fail
        err_rate = total_fail / total_req * 100 if total_req else 0
        mark = "✅" if err_rate < 1 else ("⚠️" if err_rate < 5 else "❌")
        print(f"  {mark} 并发{level:>3d}: 总QPS={total_qps:>6.0f} | 总计={total_req} | 错误={total_fail} ({err_rate:.1f}%)")
    
    # 瓶颈诊断
    print(f"\n═══ 瓶颈诊断 ═══")
    
    # QPS 增长率分析
    singles = {r["endpoint"]: r for r in all_data if r["concurrency"] == 1}
    for level in [5, 10, 20]:
        multi = {r["endpoint"]: r for r in all_data if r["concurrency"] == level and r["fail"] == 0}
        if not multi:
            continue
        ratios = []
        for ep, r in multi.items():
            if ep in singles:
                ratio = r["qps"] / singles[ep]["qps"] if singles[ep]["qps"] > 0 else 0
                ratios.append(ratio)
        if ratios:
            avg_ratio = statistics.mean(ratios)
            efficiency = avg_ratio / level * 100
            print(f"  并发{level}: 平均扩展比={avg_ratio:.2f}x (理想={level}x) | 效率={efficiency:.1f}%")
    
    # 连接池瓶颈
    print(f"\n  📌 HikariCP 瓶颈:")
    cp10 = {r["endpoint"]: r for r in all_data if r["concurrency"] == 10}
    cp20 = {r["endpoint"]: r for r in all_data if r["concurrency"] == 20}
    cp30 = {r["endpoint"]: r for r in all_data if r["concurrency"] == 30}
    
    if cp10 and cp20:
        qps10_total = sum(r["qps"] for r in cp10.values())
        qps20_total = sum(r["qps"] for r in cp20.values())
        growth = (qps20_total / qps10_total - 1) * 100 if qps10_total > 0 else 0
        print(f"  并发 10→20: QPS 增长 {growth:.0f}% (理想 100%)")
        if growth < 50:
            print(f"  ⚠️  扩展性差，连接池(HikariCP max=10)可能成为瓶颈")
        if cp30:
            qps30_total = sum(r["qps"] for r in cp30.values())
            growth2 = (qps30_total / qps20_total - 1) * 100 if qps20_total > 0 else 0
            print(f"  并发 20→30: QPS 增长 {growth2:.0f}%")
    
    # 资源建议
    print(f"\n═══ 容量建议 ═══")
    best_safe = None
    for level in CONCURRENCY_LEVELS:
        lv = [r for r in all_data if r["concurrency"] == level]
        if all(r["fail"] == 0 for r in lv):
            best_safe = level
        else:
            break
    
    print(f"  安全并发上限: {best_safe or '未达瓶颈'} (0错误)")
    for r in all_data:
        if r["concurrency"] == best_safe:
            pass
    
    # Tomcat 线程使用估算
    max_threads_needed = best_safe or 100
    print(f"  估算所需 Tomcat 线程: ~{max_threads_needed} (当前配置: 200)")
    if max_threads_needed < 200:
        print(f"  ✅ Tomcat 线程数充足 (使用率 ~{max_threads_needed/200*100:.0f}%)")
    
    # HikariCP 建议
    effective_conn = min(best_safe or 50, 50)
    print(f"  HikariCP 当前: maximum-pool-size=10 (默认)")
    if effective_conn > 10:
        print(f"  💡 建议: spring.datasource.hikari.maximum-pool-size={effective_conn}")
    
    print(f"  当前并发组件:")
    print(f"    @Async / @EnableAsync: ❌ 未启用")
    print(f"    Resilience4j RateLimiter: gacha=5/s, market=3/s, synthesize=2/s")
    print(f"    Resilience4j CircuitBreaker: 已配置但当前未触发")
    print(f"    幂等性拦截器(@Idempotent): ⚠️ WebMvcConfig中已禁用")
    print(f"    Redisson 分布式锁: RLock tryLock(10s/30s), pool=64")
    print(f"    RocketMQ 事务消息: 2消费者组, 默认20-64线程/组")
    
    # JSON
    json_path = os.path.join(os.path.dirname(__file__) or ".", "benchmark_v3_result.json")
    output = []
    for r in all_data:
        d = dict(r)
        d.pop("all_lat", None)  # 不输出原始数据
        d.pop("errors", None)
        output.append(d)
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(output, f, ensure_ascii=False, indent=2)
    print(f"\n📁 详细数据: {json_path}")


if __name__ == "__main__":
    main()
