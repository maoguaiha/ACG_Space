#!/usr/bin/env python3
"""
ACG_Space 并发能力量化测试 v2
==============================
修复: 精确 wall-clock QPS、JSON body code 检测、响应内容验证

用法:
    python concurrency_benchmark_v2.py [--quick]
"""

import concurrent.futures
import json
import os
import statistics
import sys
import threading
import time
import urllib.request
import urllib.error
from typing import Optional

BASE_URL = "http://localhost:8080"
TIMEOUT = 30

# ── 只测已验证工作的接口 ──────────────────────────────
ENDPOINTS = {
    "anime_list":      ("GET",  "/api/anime/list?page=1&size=5"),
    "featured":        ("GET",  "/api/anime/featured"),
    "item_page":       ("GET",  "/api/item/page?page=1&size=5"),
    "market_page":     ("GET",  "/api/market/page?page=1&size=5"),
    "user_profile":    ("GET",  "/api/user/1/profile"),
}

CONCURRENCY_LEVELS = [1, 5, 10, 25, 50, 100, 200]


def http_request(url, method="GET", body=None):
    """返回 (http_status, latency_ms, body_len, json_code, error)"""
    data = body.encode("utf-8") if body else None
    headers = {"Content-Type": "application/json"}
    
    start = time.perf_counter()
    try:
        req = urllib.request.Request(url, data=data, headers=headers, method=method)
        with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
            body_bytes = resp.read()
            elapsed = (time.perf_counter() - start) * 1000
            # 检测 JSON body 中的 code 字段
            json_code = resp.status
            try:
                obj = json.loads(body_bytes)
                if "code" in obj:
                    json_code = obj["code"]
            except:
                pass
            return resp.status, elapsed, len(body_bytes), json_code, ""
    except urllib.error.HTTPError as e:
        elapsed = (time.perf_counter() - start) * 1000
        try:
            blen = len(e.read())
        except:
            blen = 0
        return e.code, elapsed, blen, e.code, str(e)
    except Exception as e:
        elapsed = (time.perf_counter() - start) * 1000
        return 0, elapsed, 0, 0, str(type(e).__name__)


def run_benchmark(endpoint, method, path, concurrency, n_requests=200):
    """
    精确并发测试。
    返回: (results_list, wall_clock_seconds)
    """
    url = f"{BASE_URL}{path}"
    results = []
    results_lock = threading.Lock()
    start_barrier = threading.Barrier(concurrency + 1, timeout=60) if concurrency > 1 else None
    t0 = None
    t0_lock = threading.Lock()
    
    def worker(req_id):
        nonlocal t0
        if start_barrier:
            start_barrier.wait()  # 所有线程同时开始
        else:
            pass
        
        with t0_lock:
            if t0 is None:
                t0 = time.perf_counter()
        
        status, lat, blen, jcode, err = http_request(url, method)
        with results_lock:
            results.append({
                "http_status": status,
                "json_code": jcode,
                "latency_ms": lat,
                "body_len": blen,
                "error": err,
            })
    
    # 先创建所有 worker 线程
    threads = []
    wall_start = time.perf_counter()
    
    for i in range(n_requests):
        t = threading.Thread(target=worker, args=(i,), daemon=True)
        threads.append(t)
    
    # 分批启动，每批 concurrency 个
    for batch_start in range(0, n_requests, concurrency):
        batch = threads[batch_start:batch_start + concurrency]
        for t in batch:
            t.start()
        for t in batch:
            t.join(timeout=TIMEOUT)
    
    wall_end = time.perf_counter()
    wall_time = wall_end - wall_start
    
    return results, wall_time


def analyze(results, wall_time, endpoint_name, concurrency):
    total = len(results)
    
    # 真正成功: HTTP 2xx 且 body json code == 200
    success = [r for r in results if 200 <= r["http_status"] < 300 and r["json_code"] == 200]
    # 业务失败: HTTP 2xx 但 body json code != 200 (如 code=500)
    biz_error = [r for r in results if 200 <= r["http_status"] < 300 and r["json_code"] != 200]
    # 网络失败
    net_error = [r for r in results if r["http_status"] < 200 or r["http_status"] >= 300]
    
    success_count = len(success)
    fail_count = len(biz_error) + len(net_error)
    qps = total / wall_time if wall_time > 0 else 0
    
    latencies = sorted([r["latency_ms"] for r in results])
    
    def pct(data, p):
        if not data: return 0
        k = (len(data) - 1) * p / 100.0
        f, c = int(k), k - int(k)
        if f + 1 < len(data):
            return data[f] * (1 - c) + data[f + 1] * c
        return data[f]
    
    print(f"  {endpoint_name:<15s} | "
          f"QPS:{qps:>7.0f} | "
          f"成功:{success_count:>4d} | "
          f"业务错:{len(biz_error):>3d} | "
          f"网络错:{len(net_error):>2d} | "
          f"avg:{statistics.mean(latencies):>6.1f}ms | "
          f"P50:{pct(latencies,50):>6.1f}ms | "
          f"P90:{pct(latencies,90):>6.1f}ms | "
          f"P95:{pct(latencies,95):>6.1f}ms | "
          f"P99:{pct(latencies,99):>6.1f}ms")
    
    return {
        "endpoint": endpoint_name,
        "concurrency": concurrency,
        "total": total,
        "success": success_count,
        "biz_error": len(biz_error),
        "net_error": len(net_error),
        "qps": qps,
        "avg_ms": statistics.mean(latencies) if latencies else 0,
        "p50_ms": pct(latencies, 50),
        "p90_ms": pct(latencies, 90),
        "p95_ms": pct(latencies, 95),
        "p99_ms": pct(latencies, 99),
        "wall_time_s": wall_time,
        "error_rate": fail_count / total * 100 if total else 0,
    }


def main():
    print("""
╔══════════════════════════════════════════════════════════════╗
║     ACG_Space 并发能力量化测试 v2                            ║
║     精确 QPS | JSON code 检测 | 递增并发扫瓶颈               ║
╚══════════════════════════════════════════════════════════════╝
""")
    
    # 预热
    print("🔥 预热...")
    for _ in range(5):
        for name, (method, path) in ENDPOINTS.items():
            http_request(f"{BASE_URL}{path}", method)
    time.sleep(1)
    
    all_results = []
    
    for level in CONCURRENCY_LEVELS:
        print(f"\n{'='*90}")
        print(f"  并发等级: {level} | 每接口 200 请求 | 线程池={level}")
        print(f"{'='*90}")
        
        for ep_name, (method, path) in ENDPOINTS.items():
            sys.stdout.flush()
            results, wall_time = run_benchmark(ep_name, method, path, level, 200)
            analyzed = analyze(results, wall_time, ep_name, level)
            all_results.append(analyzed)
        
        time.sleep(1)
    
    # ── 汇总 ──
    print(f"\n\n{'#'*90}")
    print(f"{'#':>35} 📊 最终量化报告 {'#':>30}")
    print(f"{'#'*90}\n")
    
    print("═══ 各接口最佳表现 ═══")
    by_ep = {}
    for r in all_results:
        by_ep.setdefault(r["endpoint"], []).append(r)
    
    for ep_name in sorted(by_ep):
        items = by_ep[ep_name]
        best = max(items, key=lambda x: x["qps"] if x["error_rate"] < 1 else 0)
        worst_lat = max(items, key=lambda x: x["p99_ms"])
        print(f"  📌 {ep_name}")
        print(f"     峰值 QPS: {best['qps']:.0f} @并发{best['concurrency']}")
        print(f"     最优延迟: avg={best['avg_ms']:.1f}ms, P99={best['p99_ms']:.1f}ms")
        print(f"     最差 P99: {worst_lat['p99_ms']:.1f}ms @并发{worst_lat['concurrency']}")
        
        # 错误率变化
        error_rise = None
        for r in items:
            if r["error_rate"] > 1:
                error_rise = r
                break
        if error_rise:
            print(f"     ⚠️  并发{error_rise['concurrency']}时错误率达{error_rise['error_rate']:.1f}%")
    
    # 系统级
    print(f"\n═══ 系统级 ═══")
    # 查找最大安全吞吐
    safe = []
    for level in CONCURRENCY_LEVELS:
        level_results = [r for r in all_results if r["concurrency"] == level]
        if all(r["error_rate"] < 1 for r in level_results):
            total_qps = sum(r["qps"] for r in level_results)
            safe.append((level, total_qps))
    
    if safe:
        best_level, best_qps = max(safe, key=lambda x: x[1])
        print(f"  🚀 安全最大吞吐量: ~{best_qps:.0f} QPS (并发度={best_level})")
    
    # 瓶颈检测
    print(f"\n═══ 瓶颈检测 ═══")
    # HikariCP 瓶颈: 当并发 > 10 时，QPS 应受到连接池限制
    for level in [10, 25, 50, 100]:
        level_results = [r for r in all_results if r["concurrency"] == level]
        if level_results:
            total_qps = sum(r["qps"] for r in level_results)
            total_errors = sum(r["biz_error"] + r["net_error"] for r in level_results)
            print(f"  并发{level:>3d}: 总QPS={total_qps:.0f}, 总错误={total_errors}")
    
    # 资源评估
    print(f"\n═══ 资源评估 ═══")
    print(f"  当前配置:")
    print(f"    Tomcat max-threads: 200 (默认)")
    print(f"    HikariCP max-pool-size: 10 (默认)")
    print(f"    Redisson pool: 64")
    print(f"    Resilience4j 限流: gachaDraw=5/s, marketBuy=3/s, synthesizeDo=2/s")
    print(f"    RocketMQ 消费者: 2 组, 默认 20-64 线程/组")
    print(f"    @Async/@EnableAsync: 未启用")
    
    # JSON 输出
    json_path = os.path.join(os.path.dirname(__file__) or ".", "benchmark_v2_result.json")
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(all_results, f, ensure_ascii=False, indent=2)
    print(f"\n📁 详细数据: {json_path}")


if __name__ == "__main__":
    if "--quick" in sys.argv:
        CONCURRENCY_LEVELS = [1, 5, 10, 25, 50]
    main()
