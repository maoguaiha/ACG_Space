#!/usr/bin/env python3
"""
ACG_Space 并发能力量化测试脚本
================================
纯标准库实现，无需额外安装。覆盖多并发等级、核心接口，
输出 QPS、延迟分位数、错误率、成功率等关键指标。

用法:
    python concurrency_benchmark.py [--host HOST] [--port PORT]
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
from dataclasses import dataclass, field
from typing import Optional

# ── 配置 ──────────────────────────────────────────────
BASE_URL = os.environ.get("ACG_HOST", "http://localhost:8080")
TIMEOUT = 30  # 单次请求超时秒数

# ── 测试接口定义 ──────────────────────────────────────
# (名称, 路径, 方法, body, 是否需要token)
ENDPOINTS = {
    # 轻量读接口（无需认证，走 DB 查询）
    "anime_list":  ("GET",  "/api/anime/list?page=1&size=5", None),
    "featured":    ("GET",  "/api/anime/featured",     None),
    # 物品/市场 读接口
    "item_page":   ("GET",  "/api/item/page?page=1&size=5", None),
    "market_page": ("GET",  "/api/market/page?page=1&size=5", None),
    # 限流核心接口（POST，带 RateLimiter）
    "gacha_draw":  ("POST", "/api/gacha/draw",         '{"poolId":1}'),
    "market_buy":  ("POST", "/api/market/buy",         '{"listingId":1}'),
    # 认证接口
    "login":       ("POST", "/api/auth/login",        '{"username":"admin","password":"admin123"}'),
    # 用户信息
    "user_profile":("GET",  "/api/user/1/profile",     None),
}

# ── 并发等级 ──────────────────────────────────────────
CONCURRENCY_LEVELS = [1, 5, 10, 25, 50, 100, 200]

# ── 数据类 ────────────────────────────────────────────
@dataclass
class RequestResult:
    endpoint: str
    status: int
    latency_ms: float
    error: str = ""
    body_len: int = 0

@dataclass  
class BenchmarkResult:
    endpoint: str
    concurrency: int
    total: int
    success: int
    fail: int
    qps: float
    latencies: list = field(default_factory=list)
    p50: float = 0.0
    p90: float = 0.0
    p95: float = 0.0
    p99: float = 0.0
    avg: float = 0.0
    min_lat: float = 0.0
    max_lat: float = 0.0
    error_rate: float = 0.0
    errors: dict = field(default_factory=dict)


def http_request(url: str, method: str = "GET", body: Optional[str] = None,
                 headers: Optional[dict] = None) -> tuple[int, float, int, str]:
    """发送单次 HTTP 请求，返回 (状态码, 耗时ms, 响应体长度, 错误信息)"""
    data = body.encode("utf-8") if body else None
    req_headers = {"Content-Type": "application/json"}
    if headers:
        req_headers.update(headers)
    
    start = time.perf_counter()
    try:
        req = urllib.request.Request(url, data=data, headers=req_headers, method=method)
        with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
            body_bytes = resp.read()
            elapsed = (time.perf_counter() - start) * 1000
            return resp.status, elapsed, len(body_bytes), ""
    except urllib.error.HTTPError as e:
        elapsed = (time.perf_counter() - start) * 1000
        try:
            body_bytes = e.read()
            blen = len(body_bytes)
        except Exception:
            blen = 0
        return e.code, elapsed, blen, f"HTTP {e.code}"
    except Exception as e:
        elapsed = (time.perf_counter() - start) * 1000
        return 0, elapsed, 0, str(type(e).__name__)


def run_concurrency_test(endpoint_name: str, method: str, path: str, body: Optional[str],
                         concurrency: int, total_requests: int = 100) -> list:
    """以指定并发度发送 total_requests 个请求，返回 RequestResult 列表"""
    url = f"{BASE_URL}{path}"
    results = []
    results_lock = threading.Lock()

    def worker(req_id: int):
        status, lat, blen, err = http_request(url, method, body)
        with results_lock:
            results.append(RequestResult(
                endpoint=endpoint_name,
                status=status, latency_ms=lat,
                error=err, body_len=blen
            ))

    # 分批提交，每批 concurrency 个并发
    tasks = list(range(total_requests))
    
    with concurrent.futures.ThreadPoolExecutor(max_workers=concurrency) as pool:
        futures = [pool.submit(worker, i) for i in tasks]
        # 等待所有完成
        concurrent.futures.wait(futures)
    
    return results


def analyze_results(endpoint: str, concurrency: int, results: list) -> BenchmarkResult:
    """分析测试结果，计算各项指标"""
    total = len(results)
    success_list = [r for r in results if 200 <= r.status < 300]
    fail_list = [r for r in results if r.status < 200 or r.status >= 300]
    success_count = len(success_list)
    fail_count = len(fail_list)
    
    latencies = sorted([r.latency_ms for r in success_list]) if success_list else [0]
    total_time = sum(r.latency_ms for r in results) / 1000.0  # 总耗时(秒)
    
    # QPS = 成功请求数 / wall-clock 时间（近似）
    if results:
        wall_start = min(r.latency_ms for r in results)
        wall_end = max(r.latency_ms for r in results)
        wall_time = max((wall_end) / 1000.0, 0.001)  # 避免除零
        qps = total / wall_time if wall_time > 0 else 0
    else:
        qps = 0
    
    def percentile(data, p):
        if not data:
            return 0
        k = (len(data) - 1) * p / 100.0
        f = int(k)
        c = k - f
        if f + 1 < len(data):
            return data[f] * (1 - c) + data[f + 1] * c
        return data[f]
    
    # 统计错误类型
    error_counts = {}
    for r in fail_list:
        key = r.error or f"HTTP_{r.status}"
        error_counts[key] = error_counts.get(key, 0) + 1
    
    return BenchmarkResult(
        endpoint=endpoint,
        concurrency=concurrency,
        total=total,
        success=success_count,
        fail=fail_count,
        qps=qps,
        latencies=latencies,
        p50=percentile(latencies, 50),
        p90=percentile(latencies, 90),
        p95=percentile(latencies, 95),
        p99=percentile(latencies, 99),
        avg=statistics.mean(latencies) if latencies else 0,
        min_lat=min(latencies) if latencies else 0,
        max_lat=max(latencies) if latencies else 0,
        error_rate=(fail_count / total * 100) if total > 0 else 0,
        errors=error_counts,
    )


def warm_up():
    """预热：先发几轮请求让 JIT 编译、连接池初始化"""
    print("🔥 预热中...")
    for _ in range(3):
        for name, (method, path, body) in ENDPOINTS.items():
            try:
                http_request(f"{BASE_URL}{path}", method, body)
            except Exception:
                pass
    time.sleep(1)
    print("✅ 预热完成\n")


def print_separator(char="─", width=80):
    print(char * width)


def format_latency(ms):
    if ms < 1:
        return f"{ms*1000:.0f}μs"
    elif ms < 1000:
        return f"{ms:.1f}ms"
    else:
        return f"{ms/1000:.2f}s"


def print_benchmark_header(level):
    print(f"\n{'='*80}")
    print(f"  并发等级: {level}  |  每接口请求数: 100  |  总并发数: {level * len(ENDPOINTS)}")
    print(f"{'='*80}")


def print_result_row(result: BenchmarkResult):
    """单行结果输出"""
    color_ok = "\033[92m" if os.name != "nt" else ""
    color_warn = "\033[93m" if os.name != "nt" else ""
    color_reset = "\033[0m" if os.name != "nt" else ""
    
    rate_color = color_warn if result.error_rate > 0 else color_ok
    print(f"  {result.endpoint:<15s} | "
          f"总:{result.total:>3d} | "
          f"成功:{result.success:>3d} | "
          f"QPS:{result.qps:>8.1f} | "
          f"avg:{format_latency(result.avg):>8s} | "
          f"P50:{format_latency(result.p50):>8s} | "
          f"P90:{format_latency(result.p90):>8s} | "
          f"P95:{format_latency(result.p95):>8s} | "
          f"P99:{format_latency(result.p99):>8s} | "
          f"{rate_color}错误:{result.error_rate:>5.1f}%{color_reset}")


def print_final_summary(all_results: list):
    """打印最终汇总报告"""
    print(f"\n\n{'#'*80}")
    print(f"{'#':>30} 📊 最终汇总报告 {'#':>24}")
    print(f"{'#'*80}\n")
    
    # ─── 按接口汇总 ───
    print("═══ 按接口维度 ═══")
    by_endpoint = {}
    for r in all_results:
        key = r.endpoint
        if key not in by_endpoint:
            by_endpoint[key] = []
        by_endpoint[key].append(r)
    
    for ep_name, eps in sorted(by_endpoint.items()):
        best_qps = max(eps, key=lambda x: x.qps)
        best_lat = min(eps, key=lambda x: x.avg)
        worst_lat = max(eps, key=lambda x: x.p99)
        print(f"\n  📌 {ep_name}")
        print(f"     最佳 QPS: {best_qps.qps:.1f} (并发 {best_qps.concurrency})")
        print(f"     最低延迟: avg={format_latency(best_lat.avg)}, P99={format_latency(best_lat.p99)}")
        print(f"     最高延迟(P99): {format_latency(worst_lat.p99)} (并发 {worst_lat.concurrency})")
        
        # 瓶颈分析
        for r in eps:
            if r.error_rate > 5:
                print(f"     ⚠️  并发{r.concurrency}时错误率达{r.error_rate:.1f}%，可能触及吞吐上限")
                break
    
    # ─── 系统级汇总 ───
    print(f"\n═══ 系统级指标 ═══")
    # 找到最大 QPS（所有接口在最佳并发度下的 QPS 之和）
    max_total_qps = 0
    best_concurrency = 0
    for level in CONCURRENCY_LEVELS:
        level_results = [r for r in all_results if r.concurrency == level]
        total_qps = sum(r.qps for r in level_results if r.error_rate < 10)
        if total_qps > max_total_qps:
            max_total_qps = total_qps
            best_concurrency = level
    
    print(f"  🚀 系统最大吞吐量: ~{max_total_qps:.0f} QPS (并发度={best_concurrency})")
    
    # 找出第一瓶颈
    bottlenecks = []
    for r in all_results:
        if r.p95 > 3000:  # P95 > 3s
            bottlenecks.append((r.endpoint, r.concurrency, r.p95))
    if bottlenecks:
        print(f"  🔴 高延迟瓶颈 (P95 > 3s):")
        for ep, conc, p95 in sorted(bottlenecks, key=lambda x: -x[2])[:5]:
            print(f"     {ep} @并发{conc}: P95={format_latency(p95)}")
    
    # ─── 容量建议 ───
    print(f"\n═══ 容量评估与建议 ═══")
    print(f"  当前单实例安全并发度: ~{best_concurrency} (基于全接口综合测试)")
    
    # DB连接池瓶颈分析
    qps_at_50 = 0
    for r in all_results:
        if r.concurrency == 50 and r.error_rate < 5:
            qps_at_50 += r.qps
    
    print(f"  当前 HikariCP 最大连接数: 10 (默认)")
    if qps_at_50 > 50:
        print(f"  💡 建议: 在高负载下将 HikariCP maximum-pool-size 提升至 20-50")
    print(f"  当前 Tomcat 最大线程: 200 (默认)")
    print(f"  💡 建议: 若 QPS > 500，考虑增加 server.tomcat.threads.max=500")
    
    print(f"\n{'#'*80}\n")


def main():
    global BASE_URL
    
    # 解析参数
    args = sys.argv[1:]
    i = 0
    while i < len(args):
        if args[i] == "--host" and i + 1 < len(args):
            BASE_URL = args[i + 1]
            i += 2
        elif args[i] == "--port" and i + 1 < len(args):
            BASE_URL = f"http://localhost:{args[i + 1]}"
            i += 2
        elif args[i] == "--quick":
            global CONCURRENCY_LEVELS
            CONCURRENCY_LEVELS = [1, 5, 10, 25, 50]
            i += 1
        else:
            i += 1
    
    print(f"""
╔══════════════════════════════════════════════════════════╗
║        ACG_Space 并发能力量化测试                        ║
║        目标: {BASE_URL:<40s} ║
║        接口数: {len(ENDPOINTS)}  |  并发等级: {len(CONCURRENCY_LEVELS)} 个                        ║
╚══════════════════════════════════════════════════════════╝
""")
    
    # Step 0: 连通性检查
    print("🔍 连通性检查...")
    all_ok = True
    for name, (method, path, body) in ENDPOINTS.items():
        url = f"{BASE_URL}{path}"
        status, lat, _, err = http_request(url, method, body)
        icon = "✅" if 100 <= status < 400 else "❌"
        print(f"  {icon} {name:<15s} → HTTP {status} ({format_latency(lat)})" + (f" 错误:{err}" if err else ""))
        if status == 0:
            all_ok = False
    
    if not all_ok:
        print("\n⚠️  部分接口不可达，将跳过测试。请检查后端是否已启动。")
    
    # Step 1: 预热
    warm_up()
    
    # Step 2: 逐并发等级测试
    all_results = []
    
    for level in CONCURRENCY_LEVELS:
        print_benchmark_header(level)
        
        for ep_name, (method, path, body) in ENDPOINTS.items():
            sys.stdout.flush()
            
            results = run_concurrency_test(ep_name, method, path, body, level)
            analyzed = analyze_results(ep_name, level, results)
            all_results.append(analyzed)
            
            print_result_row(analyzed)
        
        # 等级间冷却（避免资源残留）
        time.sleep(1.5)
    
    # Step 3: 限流专门测试（高并发打限流接口）
    print(f"\n{'='*80}")
    print(f"  🔒 限流压测: 用高并发冲击 rate-limited 接口")
    print(f"{'='*80}")
    
    rate_limited = [k for k in ENDPOINTS if k in ("gacha_draw", "market_buy")]
    if not rate_limited:
        print("  (无限流接口可测试，跳过限流专项)")
        print_final_summary(all_results)
        return
    for ep_name in rate_limited:
        method, path, body = ENDPOINTS[ep_name]
        print(f"\n  ⚡ {ep_name}: 100并发 × 50请求 → 预期触发限流 (429)")
        results = run_concurrency_test(ep_name, method, path, body, 100, 50)
        analyzed = analyze_results(ep_name, 100, results)
        all_results.append(analyzed)
        print_result_row(analyzed)
        
        # 统计 429 响应数
        rate_limited_count = sum(1 for r in results if r.status == 429)
        print(f"     ├─ 限流触发 (HTTP 429): {rate_limited_count}/{len(results)} ({rate_limited_count/len(results)*100:.1f}%)")
        passed_count = sum(1 for r in results if 200 <= r.status < 300)
        print(f"     └─ 成功通过: {passed_count}/{len(results)} ({passed_count/len(results)*100:.1f}%)")
    
    # Step 4: 最终汇总
    print_final_summary(all_results)
    
    # 输出 JSON 结果便于后续分析
    json_path = os.path.join(os.path.dirname(__file__) or ".", "benchmark_result.json")
    json_output = []
    for r in all_results:
        json_output.append({
            "endpoint": r.endpoint,
            "concurrency": r.concurrency,
            "total": r.total,
            "success": r.success,
            "fail": r.fail,
            "qps": round(r.qps, 1),
            "avg_ms": round(r.avg, 2),
            "p50_ms": round(r.p50, 2),
            "p95_ms": round(r.p95, 2),
            "p99_ms": round(r.p99, 2),
            "min_ms": round(r.min_lat, 2),
            "max_ms": round(r.max_lat, 2),
            "error_rate_pct": round(r.error_rate, 2),
            "errors": r.errors,
        })
    
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(json_output, f, ensure_ascii=False, indent=2)
    
    print(f"\n📁 详细结果已保存至: {json_path}")


if __name__ == "__main__":
    main()
