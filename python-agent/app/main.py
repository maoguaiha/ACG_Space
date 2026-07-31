"""
ACG Space python-agent 服务入口（FastAPI，无状态 AI 大脑）。

Phase 0：打通 SSE 管线（/chat 暂为 echo 流式桩）。
Phase 2：/chat 接入 RAG 检索 + 流式 LLM（DeepSeek chat / 通义 embedding）。
Phase 3：/chat 接入只读 Bangumi 工具（function calling）+ TTL 缓存；工具失败回退 RAG。
"""
import json
import os
import re
import threading
import time

from fastapi import FastAPI
from fastapi.responses import StreamingResponse

from app.config import settings
from app.llm import chat_completion, chat_stream, embed
from app.prompts import SYSTEM_PROMPT
from app.rag import build_corpus
from app.anime_loader import ensure_anime_json
from app.schemas import ChatRequest
from app.tools.bangumi import get_airing_now, get_bangumi_detail, search_bangumi
from app.tools.registry import TOOLS

app = FastAPI(title="ACG Space Agent", version="0.3.0")

# 长对话上下文压缩：单边历史保留的最大消息条数（约 2 轮对话）。
# 之前 16（8 轮）会把 prompt 撑到 ~12K tokens，LongCat 在长 prompt + tools
# 下 TTFT 达 20-40s。压到 4 条（2 轮）后 TTFT 显著下降；需要更早内容时
# 由用户主动重述，或在 history 真摘要功能上线后补偿。
_MAX_HISTORY_MESSAGES = 4

_BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
_CORPUS_DIR = os.path.join(_BASE_DIR, "corpus")

# 尝试从后端 API 拉取番剧数据生成 anime.json（免手动部署语料文件）
# 启动时后台构建语料（uvicorn 立刻启动，健康检查不等待模型下载）。
# 创建后 corpus watcher 会定期检查并自动重建。
def _init_corpus():
    global corpus
    try:
        corpus = build_corpus(_CORPUS_DIR, embed)
        print(f"[startup] 后台语料构建完成, chunks={len(corpus.chunks)}")
    except Exception as e:  # noqa: BLE001
        print(f'[startup] 后台语料构建失败（embedding 暂不可达）：{e}')
        corpus = None

corpus = None
# 先快速拉取番剧 JSON（不依赖模型下载）
try:
    backend_url = os.getenv("BACKEND_URL", "http://backend:8080")
    ensure_anime_json(backend_url)
except Exception as e:
    print(f"[startup] 拉取番剧语料失败（{e}），跳过番剧知识源")
# 后台构建语料（含首次模型下载）
threading.Thread(target=_init_corpus, daemon=True).start()


def _corpus_dir_mtime() -> float:
    """取语料目录下所有文件的最新修改时间（用于热更新检测）。"""
    mtime = 0.0
    for root, _dirs, files in os.walk(_CORPUS_DIR):
        for f in files:
            try:
                mtime = max(mtime, os.path.getmtime(os.path.join(root, f)))
            except OSError:
                pass
    return mtime


def _start_corpus_watcher(interval: int = 30) -> None:
    """后台守护线程：轮询语料目录 mtime，变更后自动重建索引（无需手动 /rebuild 或重启）。

    用标准库轮询而非 watchdog，避免额外依赖。
    """
    global corpus
    last_mtime = _corpus_dir_mtime()

    def _loop() -> None:
        nonlocal last_mtime
        while True:
            time.sleep(interval)
            try:
                m = _corpus_dir_mtime()
                if m > last_mtime:
                    last_mtime = m
                    new_corpus = build_corpus(_CORPUS_DIR, embed)
                    corpus = new_corpus
                    print(f"[corpus-watcher] 语料已热更新，chunks={len(corpus.chunks)}")
            except Exception as e:  # noqa: BLE001
                print(f"[corpus-watcher] 重建失败：{e}")

    threading.Thread(target=_loop, daemon=True).start()


_start_corpus_watcher()


def _format_tool_results(tool_blocks):
    """把工具返回的原始 JSON 块拼成简洁 Markdown 列表。

    用于 hold-back 拦截器软 fallback：检测到工具调用 XML 时
    不再报错，而是把已经执行完的工具结果拼出来代替。
    """
    parts = []
    for blk in tool_blocks:
        try:
            head, sep, rest = blk.partition(" 返回]\n")
            if not sep:
                parts.append(blk)
                continue
            name = head.lstrip("[工具 ")
            data = json.loads(rest) if rest.strip() else {}
        except Exception:
            parts.append(f"- (工具返回解析失败: {blk[:80]!r})")
            continue
        if isinstance(data, list):
            for item in data[:8]:
                if not isinstance(item, dict):
                    continue
                title = item.get("title") or item.get("name") or "未呷名"
                year = item.get("year") or item.get("publishYear") or item.get("publish_year")
                rating = item.get("rating")
                url = item.get("url")
                line = f"- **{title}**"
                if year:
                    line += f" ({year})"
                if rating is not None:
                    try:
                        line += f" ⭐{round(float(rating), 1)}"
                    except (TypeError, ValueError):
                        pass
                if url:
                    line += f"  {url}"
                parts.append(line)
        elif isinstance(data, dict):
            # 常见包裹字段：items / data / list / results（搜类工具通常把
            # 结果放在 items 下，外层再包 count / query 等元数据）
            inner = None
            for key in ("items", "data", "list", "results"):
                v = data.get(key)
                if isinstance(v, list):
                    inner = v
                    break
            if inner is not None:
                # 递归处理——把 list 当作顶层处理
                for item in inner[:8]:
                    if not isinstance(item, dict):
                        continue
                    # name_cn（中文名）优先，缺失才回退到 name（日文）
                    title = item.get("name_cn") or item.get("title") or item.get("name") or "未题名"
                    year = item.get("year") or item.get("publishYear") or item.get("publish_year")
                    rating = item.get("rating")
                    url = item.get("url")
                    image = item.get("image") or item.get("cover") or ""
                    # 封面图（独立行，紧凑 Markdown 渲染）
                    if image:
                        parts.append(f"![cover]({image})")
                    line = f"- **{title}**"
                    if year:
                        line += f" ({year})"
                    if rating is not None:
                        try:
                            line += f" ⭐{round(float(rating), 1)}"
                        except (TypeError, ValueError):
                            pass
                    # 简介（取 summary 前 80 字）
                    summary = item.get("summary") or ""
                    if summary:
                        line += f"\n  简介：{summary[:80]}{'…' if len(summary) > 80 else ''}"
                    if url:
                        line += f"\n  [Bangumi 详情]({url})"
                    parts.append(line)
            else:
                title = data.get("name_cn") or data.get("title") or data.get("name") or ""
                if title:
                    parts.append(f"- **{title}**")
        else:
            parts.append(f"- (工具 {name} 返回不可预期类型: {type(data).__name__})")
    return "\n".join(parts) if parts else "(工具未返回可展示的结果)"


def _tool_names(tool_blocks):
    """从工具返回块里提取展示名称列表（中文名优先）。

    用于 LLM 评论阶段：只给名称列表，不给 JSON/格式化文本，
    避免模型重复输出番剧卡片。
    """
    names = []
    for blk in tool_blocks:
        try:
            head, sep, rest = blk.partition(" 返回]\n")
            if not sep:
                continue
            data = json.loads(rest) if rest.strip() else {}
        except Exception:
            continue
        inner = None
        if isinstance(data, dict):
            for key in ("items", "data", "list", "results"):
                v = data.get(key)
                if isinstance(v, list):
                    inner = v
                    break
        elif isinstance(data, list):
            inner = data
        for item in (inner or []):
            if not isinstance(item, dict):
                continue
            nm = item.get("name_cn") or item.get("title") or item.get("name")
            if nm and nm not in names:
                names.append(nm)
    return names


def _sse(obj: dict) -> str:
    """构造 SSE data 帧（与前端 useAgentApi 的解析约定一致：data:{json}\\n\\n）。"""
    return f"data:{json.dumps(obj, ensure_ascii=False)}\n\n"


def _parse_longcat_tool_calls(content: str) -> list[tuple[str, dict]]:
    """从 LongCat 自定义 XML 格式里抠出工具调用。

    LongCat（LLM_CHAT_MODEL）不支持 OpenAI 标准 function-calling 的 tool_calls 字段，
    而是把工具调用写成自有的 XML 标记，直接塞进回复 content：
        <longcat_tool_call>search_bangumi
        <longcat_arg_key>keyword</longcat_arg_key>
        <longcat_arg_value>推理</longcat_arg_value>
        <longcat_arg_key>limit</longcat_arg_key>
        <longcat_arg_value>8</longcat_arg_value>
        </longcat_tool_call>

    返回 [(工具名, 参数字典), ...]；无 XML 时返回空列表。
    """
    calls: list[tuple[str, dict]] = []
    for block in re.finditer(
        r"<longcat_tool_call>(.*?)</longcat_tool_call>", content, re.DOTALL
    ):
        inner = block.group(1)
        lines = [ln.strip() for ln in inner.strip().splitlines() if ln.strip()]
        if not lines:
            continue
        name = lines[0]  # 工具名在首行
        args: dict = {}
        # finditer 返回的是单个 Match 对象迭代器，不能解包成两个变量
        for m in re.finditer(
            r"<longcat_arg_key>(.*?)</longcat_arg_key>\s*"
            r"<longcat_arg_value>(.*?)</longcat_arg_value>",
            inner,
            re.DOTALL,
        ):
            args[m.group(1).strip()] = m.group(2).strip()
        calls.append((name, args))
    return calls


def _parse_agnes_tool_calls(content: str) -> list[tuple[str, dict]]:
    """从 Agnes AI 的 XML 格式里抠出工具调用。

    Agnes AI（LLM_CHAT_MODEL）也不走 OpenAI 标准 tool_calls,而是把工具调用写成
    自有 XML 标签直接塞进 content:
        <tool_call>
        <function=get_bangumi_detail>
        <parameter=id>
        274613
        </parameter>
        </function>
        </tool_call>

    返回 [(工具名, 参数字典), ...];无 XML 时返回空列表。
    """
    calls: list[tuple[str, dict]] = []
    # 预处理：剥离常见零宽/不可见字符(ZWSP/ZWNJ/ZWJ/BOM/LRM/RLM)
    # 让 XML 标签格式微调也能匹配
    _ZERO_WIDTH = "\u200b\u200c\u200d\ufeff\u200e\u200f"
    content = content.translate(str.maketrans("", "", _ZERO_WIDTH))
    for block in re.finditer(
        r"<tool_call>(.*?)</tool_call>", content, re.DOTALL
    ):
        inner = block.group(1)
        # 工具名在 <function=NAME> 里
        name_m = re.search(r"<function=([^\s>]+)>", inner)
        if not name_m:
            continue
        name = name_m.group(1).strip()
        # 参数: <parameter=KEY>...VALUE...</parameter>
        args: dict = {}
        for pm in re.finditer(
            r"<parameter=([^>]+)>(.*?)</parameter>",
            inner,
            re.DOTALL,
        ):
            key = pm.group(1).strip()
            val = pm.group(2).strip()
            args[key] = val
        calls.append((name, args))
    return calls


def _run_tool(name: str, args: dict, retries: int = 2) -> dict:
    """分发只读 Bangumi 工具；失败自动重试，仍失败则转 error 结果交由 LLM 回退。

    retries: 额外重试次数（默认 2，共最多 3 次尝试）。
    """
    last_err: Exception | None = None
    for _ in range(retries + 1):
        try:
            if name == "search_bangumi":
                return search_bangumi(**args)
            if name == "get_bangumi_detail":
                return get_bangumi_detail(**args)
            if name == "get_airing_now":
                return get_airing_now()
            return {"error": f"未知工具：{name}"}
        except Exception as e:  # noqa: BLE001
            last_err = e
            continue
    return {"error": f"工具 {name} 调用失败（重试 {retries} 次后仍失败）：{last_err}"}


# 意图预判：用户消息是否可能涉及番剧工具调用。
# 过去每次对话都先跑一轮 non-streaming 探测
# (chat_completion 全量生成一遍来判断要不要调工具)，
# LongCat 不支持标准 tool_calls，探测 = 完整生成，5-15s。
# 这是“大几十秒”的最大来源。
# 此函数用关键词判断：非番剧问题直接跳过探测，
# 只走一次 streaming（简单回复 4-12s）。
_TOOL_HINT_RE = re.compile(
    r"番剧|新番|动画|动漫|评分|bangumi|放送|开播|"
    r"追番|声优|CV|剧集|番名|动画翻|电影版|"
    r"推荐|类似|像|治愈|热血|机战|"
    r"恋爱番|搞笑番|悬疑番|正太番|"
    r"这[季周]|本季|本[周月]|几集|多少集|"
    r"哪部|有没有好看的|"
    r"这部|那部|什么番|哪部番|"
    r"搜|《"  # 搜=搜索意图；《=书名号（番名/作品名，如《葬送的芙莉莲》）
)


def _needs_tools(message: str) -> bool:
    """判断当前消息是否可能需要调用番剧工具。

    命中番剧相关关键词 -> True（走完整工具流程）；
    否则 False（跳过探测，直接一次 streaming）。
    """
    return bool(_TOOL_HINT_RE.search(message or ""))


@app.get("/health")
def health():
    return {
        "status": "ok",
        "corpus_ready": corpus is not None,
        "chunks": len(corpus.chunks) if corpus else 0,
    }


@app.post("/rebuild")
def rebuild():
    """重新构建 RAG 索引（规则/FAQ/番剧快照变更后调用，或重启服务）。"""
    global corpus
    try:
        corpus = build_corpus(_CORPUS_DIR, embed)
    except Exception as e:  # noqa: BLE001
        return {"status": "error", "detail": str(e)}
    return {"status": "rebuilt", "chunks": len(corpus.chunks) if corpus else 0}


@app.post("/chat")
async def chat(req: ChatRequest):
    """SSE 流式对话：RAG 检索 → 拼系统提示 → LLM（带 Bangumi 工具）→ 工具执行 → 流式回答。"""

    async def event_stream():
        global corpus  # event_stream 内部需要重新赋值 corpus,Python 闭包规则必须显式声明
        # 探测阶段可能不进入 XML 工具路径（LongCat 直接返回 content="" 或空
        # 消息），所以 tool_blocks fallback 列表必须在入口先初始化，否则下面
        # `if _tool_blocks_for_fallback:` 触发时会报 UnboundLocalError。
        # 同时：禁止在任何分支对它做 `= []` 重新赋值，否则 Python 把整个函数
        # 的这个名都当 local，导致这里的初始化被遮蔽。
        _tool_blocks_for_fallback: list[str] = []
        try:
            # 1) 检索相关分块（语料未就绪时先尝试懒加载一次；
            #    仍失败则降级为纯 LLM 问答，context 置空，不中断对话）
            context = ""
            if corpus is None:
                try:
                    corpus = build_corpus(_CORPUS_DIR, embed)
                except Exception as e:  # noqa: BLE001
                    print(f"[chat] 语料不可用，降级纯 LLM 问答：{e}")
            if corpus is not None:
                try:
                    chunks = corpus.search(req.message, top_k=3)
                    context = "\n\n".join(
                        f"[来源:{c.source} / {c.title}]\n{c.text}" for c in chunks
                    )
                except Exception as e:  # noqa: BLE001
                    print(f"[chat] RAG 检索失败，降级纯 LLM 问答：{e}")
            messages = [
                {"role": "system", "content": SYSTEM_PROMPT.format(context=context)}
            ]
            # 1.5) 长对话上下文压缩：历史过长时只保留最近若干条，
            # 更早的以一条系统备注概括数量，避免逼近 token 上限导致报错。
            history = list(req.history)
            omitted = 0
            if len(history) > _MAX_HISTORY_MESSAGES:
                omitted = len(history) - _MAX_HISTORY_MESSAGES
                history = history[-_MAX_HISTORY_MESSAGES:]
                messages.append(
                    {
                        "role": "system",
                        "content": (
                            f"（已省略较早的 {omitted} 条历史消息，仅保留最近 "
                            f"{_MAX_HISTORY_MESSAGES} 条以维持上下文精简；"
                            "若需回顾更早内容，请用户主动提及。）"
                        ),
                    }
                )
            for h in history:
                messages.append({"role": h.role, "content": h.content})
            # 附件（V1 仅文本）：作为一条 user 消息注入，让 LLM 能引用文件内容
            att = req.attachment
            if att and att.content:
                # 安全上限：超长文本截断，避免逼近 token 上限导致报错
                MAX_ATTACH_CHARS = 30000
                content = att.content
                if len(content) > MAX_ATTACH_CHARS:
                    content = content[:MAX_ATTACH_CHARS] + "\n…（内容过长已截断）"
                messages.append(
                    {
                        "role": "user",
                        "content": (
                            f"【用户上传的文件《{att.filename or '未命名'}》内容如下，"
                            f"请基于它回答我的问题】\n{content}"
                        ),
                    }
                )
            messages.append({"role": "user", "content": req.message})

            # 2) 工具探测：仅当用户消息可能涉及番剧时才执行。
            # 过去每次对话都先跑一轮 non-streaming 探测，
            # LongCat 不支持标准 tool_calls，探测 = 完整生成一遍，
            # 即使简单规则问题也白等 5-15s。用意图预判跳过：
            # 非番剧问题直接走一次 streaming（不传 tools），回复 4-12s。
            if _needs_tools(req.message):
                # 2) 首轮：带 tools，非流式，检测 tool_calls。
                # 工具模式异常（模型不稳定等）时降级为无工具纯对话，让模型基于 RAG 直接答。
                try:
                    first = chat_completion(
                        messages,
                        tools=TOOLS,
                        model=req.model,
                        temperature=req.temperature,
                    )
                except Exception:
                    first = chat_completion(
                        messages,
                        model=req.model,
                        temperature=req.temperature,
                    )
                assistant_msg = first.choices[0].message
                tool_calls = getattr(assistant_msg, "tool_calls", None)

                if tool_calls:
                    # 回传 assistant 的 tool_calls 消息（OpenAI 要求）
                    yield _sse({"type": "tool_status", "content": "正在查询番剧库…"})
                    messages.append(
                        {
                            "role": "assistant",
                            "content": assistant_msg.content or "",
                            "tool_calls": [tc.model_dump() for tc in tool_calls],
                        }
                    )
                    for tc in tool_calls:
                        args = json.loads(tc.function.arguments or "{}")
                        result = _run_tool(tc.function.name, args)
                        # 同时填充 _tool_blocks_for_fallback：LongCat 即便走
                        # 标准 tool_calls 路径，streaming 阶段仍可能再吐 XML，
                        # 拦截器 fallback 用工具结果时不能漏
                        _tool_blocks_for_fallback.append(
                            f"[工具 {tc.function.name} 返回]\n{json.dumps(result, ensure_ascii=False)}"
                        )
                        messages.append(
                            {
                                "role": "tool",
                                "tool_call_id": tc.id,
                                "content": json.dumps(result, ensure_ascii=False),
                            }
                        )
                    yield _sse({"type": "tool_status", "content": ""})

                    # 双轨输出：
                    # 1) 先 yield 格式化工具结果（封面图/中文名/评分/剧情简介/链接，100% 有图有信息）
                    # 2) 追加 system 指令让 LLM 写一段自然语言评论（不重复列表），然后继续走
                    #    #3 流式最终回答 —— 既有质量保证的结构化信息，又有 LLM 自由评论。
                    if _tool_blocks_for_fallback:
                        yield _sse({
                            "type": "token",
                            "content": _format_tool_results(_tool_blocks_for_fallback)
                            + "\n\n（来源：Bangumi）",
                        })
                    # 评论阶段：过滤掉 tool 消息（避免 LLM 从 JSON 复述卡片），
                    # 只留 system/user/assistant 文本 + 番剧名称列表。
                    _names = _tool_names(_tool_blocks_for_fallback)
                    if _names:
                        messages = [m for m in messages if m.get("role") != "tool" and not m.get("tool_calls")]
                        messages.append(
                            {
                                "role": "system",
                                "content": (
                                    "番剧信息已在上方展示给用户（封面/名称/评分/简介/链接）。"
                                    "请对以下番剧写一段 100~200 字的自然语言评论，点评看点、"
                                    "亮点、适合人群或相互对比。严禁重复列出番剧名称、"
                                    "评分、链接、图片或简介——只输出你的评论文字。"
                                ),
                            }
                        )
                        messages.append(
                            {"role": "user", "content": "番剧名称列表：" + "、".join(_names)}
                        )
                    # 不 return：继续走 #3 流式最终回答，LLM 生成评论

                elif assistant_msg.content:
                    # 自定义 XML 工具调用兜底：部分模型（LongCat、Agnes AI 等）不走
                    # OpenAI 标准 tool_calls,而是把工具调用写成 XML 标签塞进 content。
                    # 按顺序尝试 LongCat 和 Agnes 两种格式,谁先匹配上就执行谁。
                    xml_calls = _parse_longcat_tool_calls(assistant_msg.content)
                    if not xml_calls:
                        xml_calls = _parse_agnes_tool_calls(assistant_msg.content)
                    if xml_calls:
                        # 清空原始 XML 内容，避免它作为「回答」流到前端
                        yield _sse({"type": "tool_status", "content": "正在查询番剧库…"})
                        assistant_msg.content = ""
                        # 注意：不要写 `_tool_blocks_for_fallback = []`，
                        # 否则 Python 把整个函数内的它标 local，顶部初始化失效、
                        # 未走 XML 路径时 if _tool_blocks_for_fallback 报
                        # UnboundLocalError。顶部 event_stream 入口已初始化一次，
                        # 此处只 append 即可。
                        for name, args in xml_calls:
                            result = _run_tool(name, args)
                            _tool_blocks_for_fallback.append(
                                f"[工具 {name} 返回]\n{json.dumps(result, ensure_ascii=False)}"
                            )
                        # 非原生 tool-calling 模型：把工具结果作为 user 消息回填，
                        # 要求模型直接基于结果作答（不要重复调用、不要输出 XML）。
                        messages.append(
                            {
                                "role": "user",
                                "content": (
                                    "以下是你刚才请求的工具调用的返回结果，请基于它直接回答用户的问题"
                                    "（不要重复调用工具，也不要输出任何 XML 标签）：\n\n"
                                    + "\n\n".join(_tool_blocks_for_fallback)
                                ),
                            }
                        )
                        # 追加一条高优先级的 system 压制指令：工具结果已拿到，
                        # 本次 streaming 必须直接输出最终回答，禁止再吐工具调用 XML。
                        # 不加这条，LongCat 会在最终回答阶段再次输出 <longcat_tool_call>
                        # （已被 hold-back 拦截器拦截并报错，用户看不到答案）。
                        messages.append(
                            {
                                "role": "system",
                                "content": (
                                    "工具调用已经完成，工具返回结果已附在上一条用户消息中。"
                                    "现在请直接基于这些结果用 Markdown 组织最终回答。"
                                    "绝对禁止再次输出任何 XML 工具调用标签"
                                    "（如 <longcat_tool_call>、<tool_call>），禁止再次调用工具。"
                                ),
                            }
                        )
                        yield _sse({"type": "tool_status", "content": ""})

                        # 双轨输出（同 if tool_calls 分支）：先 yield 格式化工具结果，
                        # 再让 LLM 流式生成自然语言评论。
                        if _tool_blocks_for_fallback:
                            yield _sse({
                                "type": "token",
                                "content": _format_tool_results(_tool_blocks_for_fallback)
                                + "\n\n（来源：Bangumi）",
                            })
                        # 评论阶段：过滤 tool 消息 + 只给番剧名称列表，避免模型重复输出卡片。
                        _names = _tool_names(_tool_blocks_for_fallback)
                        if _names:
                            messages = [m for m in messages if m.get("role") != "tool" and not m.get("tool_calls")]
                            messages.append(
                                {
                                    "role": "system",
                                    "content": (
                                        "番剧信息已在上方展示给用户（封面/名称/评分/简介/链接）。"
                                        "请对以下番剧写一段 100~200 字的自然语言评论，点评看点、"
                                        "亮点、适合人群或相互对比。严禁重复列出番剧名称、"
                                        "评分、链接、图片或简介——只输出你的评论文字。"
                                    ),
                                }
                            )
                            messages.append(
                                {"role": "user", "content": "番剧名称列表：" + "、".join(_names)}
                            )
                        # 不 return：继续走 #3 流式最终回答


            # 3) 流式最终回答（无论是否经过工具，统一走流式）
            #
            # 安全间隔：LongCat / Agnes AI 都会在 streaming 阶段吐出 XML 工具调用。
            # 单 token 子串检测 + 简单 buffer 都会被跨 token 切碎绕过，且会把
            # 部分开标签前缀（如 "<longcat_tool_"）直接 yield 到前端。
            # 采用 "hold-back" 策略：每次收到 token 先入缓冲，找到 buffer 末尾
            # 最后一个 "<"，把它之前的安全区段 yield 出去，"<" 及之后的内容
            # 扣住暂不发送，直到下一 token 进来确认它不是开标签。这样
            # 引入最多一个 token 长度的人眼不可感知延迟，但保证零 XML 泄露。
            #   1) 开标签识别："<longcat_tool_call" 与 "<tool_call>"（Agnes 含 U+200B）；
            #   2) 命中即 yield 错误并 break，stream 进入 done；
            #   3) 流末残留的疑似 "<" 开头片段也丢弃，避免截屏看到半截标签。
            _xml_buf = ""
            _xml_hit = False
            _MAX_LOOKAHEAD = 32  # 最长开标签 18 字符，留余量
            for token in chat_stream(
                messages,
                model=req.model,
                temperature=req.temperature,
            ):
                if _xml_hit:
                    # 已检测到开标签，后续 token 全部丢弃，直到 stream 结束
                    continue
                _xml_buf += token
                # 一旦 buffer 出现开标签 -> 软 fallback（不报错）
                # 先去除 U+FF5C（全角反斜杠）和零宽字符，再用正则匹配。
                # DeepSeek 实测会输出 <｜｜DSML｜｜tool_calls> 这样的低调标签；
                # 正则 <[^a-zA-Z]*(tool_calls|invoke|...) 宽松匹配不依赖精确子串。
                _xml_norm = re.sub(r"[\u200b-\u200f\ufeff\uff5c]", "", _xml_buf)
                _xml_hit_tag = re.search(
                    r"<[^<>]{0,40}(tool_calls|invoke|longcat_tool_call|tool_call)", _xml_norm
                )
                if _xml_hit_tag:
                    _xml_hit = True
                    # 软 fallback：不再报错。探测阶段如果走过工具路径，
                    # 用工具结果拼 Markdown 回答；否则 yield 提示 + done。
                    if _tool_blocks_for_fallback:
                        yield _sse({"type": "tool_status", "content": "（模型在流式阶段重复调用工具，已用首次结果直接呈现）"})
                        yield _sse({"type": "token", "content": _format_tool_results(_tool_blocks_for_fallback)})
                    else:
                        yield _sse({"type": "tool_status", "content": "（模型输出异常，已自动截断）"})
                    break
                # hold-back：找到 buffer 末尾最后一个 "<"，它之前部分安全可以发送
                _safe_until = max(0, len(_xml_buf) - _MAX_LOOKAHEAD)
                _last_lt = _xml_buf.rfind("<", _safe_until)
                if _last_lt < 0:
                    # 末尾 32 字符内没有 "<"，整段安全
                    yield _sse({"type": "token", "content": _xml_buf})
                    _xml_buf = ""
                else:
                    yield _sse({"type": "token", "content": _xml_buf[:_last_lt]})
                    _xml_buf = _xml_buf[_last_lt:]
                    if len(_xml_buf) > _MAX_LOOKAHEAD * 2:
                        _xml_buf = _xml_buf[-_MAX_LOOKAHEAD:]
            # 流结束：若 buffer 还有残留疑似 "<" 开头片段，丢弃
            if not _xml_hit and _xml_buf:
                _xml_norm = re.sub(r"[\u200b-\u200f\ufeff\uff5c]", "", _xml_buf)
                _xml_hit_tag = re.search(
                    r"<[^<>]{0,40}(tool_calls|invoke|longcat_tool_call|tool_call)", _xml_norm
                )
                if _xml_hit_tag or _xml_norm.lstrip().startswith("<"):
                    # 软 fallback：流末残留疑似开标签也不报错
                    if _tool_blocks_for_fallback:
                        yield _sse({"type": "tool_status", "content": "（流末检测到疑似 XML，已用工具结果呈现）"})
                        yield _sse({"type": "token", "content": _format_tool_results(_tool_blocks_for_fallback)})
                    else:
                        yield _sse({"type": "tool_status", "content": "（流末检测到疑似开标签，已自动截断）"})
                else:
                    yield _sse({"type": "token", "content": _xml_buf})
        except RuntimeError as e:
            # 配置缺失（缺 API Key）等可预期错误：明确提示，便于排查
            yield _sse({"type": "error", "content": f"配置或依赖缺失：{e}"})
        except Exception as e:  # noqa: BLE001
            # 其余异常兜底，保证流正常结束
            yield _sse({"type": "error", "content": f"服务暂不可用：{e}"})
        yield _sse({"type": "done", "content": ""})

    return StreamingResponse(event_stream(), media_type="text/event-stream")
