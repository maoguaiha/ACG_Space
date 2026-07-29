<script setup lang="ts">
/**
 * 消息列表 + 自动滚动底部的聊天窗口。
 *
 * 特性：
 *   - 空态：首次进入显示欢迎引导 + 快捷提示词
 *   - 已有消息：内联渲染用户/助手气泡（AI 消息用 markdown-it 渲染）
 *   - 思考中：流式尚未返回首个 token 时，显示三点跳动指示器 + 累计秒数
 *   - 流式进行中：就地渲染 Markdown + 闪烁光标
 *   - 自动滚动：仅在用户已处于底部时自动跟随，否则保持原位不打扰阅读
 */
import { ref, reactive, watch, computed, nextTick, onUnmounted, onMounted } from 'vue'
import MarkdownIt from 'markdown-it'

const props = defineProps<{
  /** 当前会话 ID，变化时重置滚动位置（回到顶部，不强制跟随底部） */
  conversationId: string | null
  messages: Array<{
    id: string
    role: 'user' | 'assistant'
    content: string
    isError?: boolean
  }>
  hasStreaming: boolean
  streamingContent: string
  /** 工具执行中提示（python-agent 发来的 tool_status 事件，空串表示结束） */
  toolStatus?: string
  /** 会话内搜索关键词（仅前端过滤展示） */
  searchQuery?: string
  /** 反馈记录：messageId -> 'up' | 'down' */
  feedbackMap?: Record<string, 'up' | 'down'>
}>()

const emit = defineEmits<{
  'quick-send': [q: string]
  feedback: [msgId: string, type: 'up' | 'down']
  regenerate: []
  'update:searchQuery': [value: string]
}>()

// 搜索过滤后的展示列表（无关键词时展示全部）
const displayMessages = computed(() => {
  const q = (props.searchQuery || '').trim().toLowerCase()
  if (!q) return props.messages
  return props.messages.filter((m) => (m.content || '').toLowerCase().includes(q))
})

// ---------- Markdown 渲染器 ----------
// html:false  阻止源 Markdown 中的原始 HTML 透传（防 XSS）
// linkify:true 自动识别 URL 并转链接
// breaks:true  把 \n 转 <br>（流式部分内容更友好）
const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  typographer: false,
})

// 所有链接强制新窗口打开 + 防 referrer/tab-nabbing
const _defaultLinkOpen = md.renderer.rules.link_open
  || function (tokens, idx, options, env, self) { return self.renderToken(tokens, idx, options) }
md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  const token = tokens[idx]
  const hrefIdx = token.attrIndex('href')
  if (hrefIdx >= 0) {
    const href = token.attrs?.[hrefIdx]?.[1] ?? ''
    if (/^https?:\/\//i.test(href)) {
      token.attrSet('target', '_blank')
      token.attrSet('rel', 'noopener noreferrer')
    }
  }
  return _defaultLinkOpen(tokens, idx, options, env, self)
}

// 流式内容响应式渲染
const renderedStreaming = computed(() => md.render(props.streamingContent || ''))
function renderMarkdown(content: string): string {
  return md.render(content || '')
}

// ---------- 锚点 / 滚动 ----------
const anchorRef = ref<HTMLElement>()
const scrollContainerRef = ref<HTMLElement>()

/** 思考累计秒数（hasStreaming 变 true 起 1s 一跳；变 false 清零） */
const thinkingSeconds = ref(0)
let thinkingTimer: ReturnType<typeof setInterval> | null = null

function startThinkingTimer() {
  if (thinkingTimer) return
  thinkingSeconds.value = 0
  thinkingTimer = setInterval(() => { thinkingSeconds.value++ }, 1000)
}

function stopThinkingTimer() {
  if (thinkingTimer) {
    clearInterval(thinkingTimer)
    thinkingTimer = null
  }
  thinkingSeconds.value = 0
}

// 跟随 isStreaming 启停计时器
watch(() => props.hasStreaming, (streaming) => {
  if (streaming) startThinkingTimer()
  else stopThinkingTimer()
}, { immediate: true })

onUnmounted(stopThinkingTimer)

/** 自动跟随：仅在用户已处于底部时滚动到底，避免阅读历史时被打断 */
const stickToBottom = ref(true)
function checkStickToBottom() {
  const el = scrollContainerRef.value
  if (!el) return
  const distFromBottom = el.scrollHeight - el.scrollTop - el.clientHeight
  stickToBottom.value = distFromBottom < 80
  // 同步：正在滚动（用于细滚动条淡入）
  isScrolling.value = true
  scheduleScrollIdle()
  updateScrollMetrics()
}

/** 滚动条可见性：滚动时浮现，停 1.5s 后淡出（macOS overlay 风格） */
const isScrolling = ref(false)
let scrollIdleTimer: ReturnType<typeof setTimeout> | null = null
function scheduleScrollIdle() {
  if (scrollIdleTimer) clearTimeout(scrollIdleTimer)
  // 停止滚动 1.5 秒后滑块淡出（macOS overlay 约 1~2s）
  scrollIdleTimer = setTimeout(() => { isScrolling.value = false }, 1500)
}
onUnmounted(() => { if (scrollIdleTimer) clearTimeout(scrollIdleTimer) })

// ==================== 自绘 mac 风格细滑块 ====================
/** 滚动容器度量（驱动自绘滑块的尺寸与位置） */
const scrollMetrics = reactive({ scrollTop: 0, clientHeight: 0, scrollHeight: 0 })
/** 内容是否溢出（决定要不要渲染滑块） */
const canScroll = computed(() => scrollMetrics.scrollHeight - scrollMetrics.clientHeight > 4)
/** 滑块（thumb）高度：按比例缩放，最小 24px、最大不超过容器 */
const thumbHeight = computed(() => {
  const { clientHeight, scrollHeight } = scrollMetrics
  if (!scrollHeight || clientHeight >= scrollHeight) return 0
  const h = Math.floor((clientHeight / scrollHeight) * clientHeight)
  return Math.max(24, Math.min(h, clientHeight))
})
/** 滑块纵向位置（translateY 像素） */
const thumbTop = computed(() => {
  const { scrollTop, clientHeight, scrollHeight } = scrollMetrics
  const maxScroll = scrollHeight - clientHeight
  if (maxScroll <= 0) return 0
  const track = Math.max(1, clientHeight - thumbHeight.value - 16) // 上下各留 8px
  return Math.round((scrollTop / maxScroll) * track)
})
function updateScrollMetrics() {
  const el = scrollContainerRef.value
  if (!el) return
  scrollMetrics.scrollTop = el.scrollTop
  scrollMetrics.clientHeight = el.clientHeight
  scrollMetrics.scrollHeight = el.scrollHeight
}
/** 拖拽滑块滚动 */
let thumbDragging = false
let dragStartY = 0
let dragStartScrollTop = 0
function onThumbPointerDown(e: PointerEvent) {
  const el = scrollContainerRef.value
  if (!el) return
  thumbDragging = true
  dragStartY = e.clientY
  dragStartScrollTop = el.scrollTop
  window.addEventListener('pointermove', onThumbPointerMove)
  window.addEventListener('pointerup', onThumbPointerUp)
  e.preventDefault()
}
function onThumbPointerMove(e: PointerEvent) {
  if (!thumbDragging) return
  const el = scrollContainerRef.value
  if (!el) return
  const denom = (el.clientHeight - thumbHeight.value) || 1
  const scrollPerPx = (el.scrollHeight - el.clientHeight) / denom
  el.scrollTop = dragStartScrollTop + (e.clientY - dragStartY) * scrollPerPx
}
function onThumbPointerUp() {
  thumbDragging = false
  window.removeEventListener('pointermove', onThumbPointerMove)
  window.removeEventListener('pointerup', onThumbPointerUp)
}
onUnmounted(() => {
  window.removeEventListener('pointermove', onThumbPointerMove)
  window.removeEventListener('pointerup', onThumbPointerUp)
})
/** 初次挂载 / 内容变化 / 视口变化时刷新度量 */
function refreshMetrics() { nextTick(updateScrollMetrics) }
onMounted(() => { refreshMetrics(); window.addEventListener('resize', refreshMetrics) })
onUnmounted(() => { window.removeEventListener('resize', refreshMetrics) })
watch(() => props.messages.length, refreshMetrics)
watch(() => props.conversationId, refreshMetrics)

function scrollToBottom() {
  if (!stickToBottom.value) return
  nextTick(() => {
    const el = scrollContainerRef.value
    if (el) el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
  })
}

/** 切换会话：回到顶部且暂停底部跟随，避免历史长会话被强制下滚 */
watch(() => props.conversationId, () => {
  stickToBottom.value = false
  nextTick(() => {
    scrollContainerRef.value?.scrollTo({ top: 0, behavior: 'auto' })
    // 重置 scroll-spy：IntersectionObserver 下一轮再算
    activeUserIdx.value = 0
  })
}, { immediate: true })

/** 用户主动发送消息时由父组件触发：恢复底部跟随并立即滚到底部 */
function forceScrollToBottom() {
  stickToBottom.value = true
  nextTick(() => {
    const el = scrollContainerRef.value
    if (el) el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
  })
}

defineExpose({ forceScrollToBottom })

watch(() => props.messages.length, () => { checkStickToBottom(); scrollToBottom() })
watch(() => props.streamingContent, () => { scrollToBottom() })

// ==================== 千问式右侧小横杠 scroll-spy ====================
/** 用户问题序列（按消息顺序，仅 role==='user'） */
const userQuestions = computed(() => props.messages.filter((m) => m.role === 'user'))
const questionIdxs = computed(() => userQuestions.value.map((_, i) => i))

/** 预计算 userQuestion.id → 在 userQuestions 里的 idx（避免 v-for 里再 findIndex） */
const userIdxById = computed<Record<string, number>>(() => {
  const map: Record<string, number> = {}
  userQuestions.value.forEach((m, i) => { map[m.id] = i })
  return map
})

/** 当前可见的用户问题在 userQuestions 里的索引 */
const activeUserIdx = ref(0)

/** 点击右侧小横杠：滚到对应用户问题（顶部留 14px ≈ pt-14） */
function scrollToUserQuestion(idx: number) {
  const el = scrollContainerRef.value
  if (!el) return
  // 找对应消息的真实 DOM：用 [data-user-idx] 选择器
  const target = el.querySelector<HTMLElement>(`[data-user-idx="${idx}"]`)
  if (!target) {
    // 兜底：若因 TransitionGroup 渲染延迟找不到，按 idx 推算列表里的第 idx 条用户消息
    const allUserNodes = el.querySelectorAll<HTMLElement>('[data-user-idx]')
    const byOrder = allUserNodes[idx]
    if (!byOrder) return
    el.scrollTo({ top: byOrder.offsetTop - 14, behavior: 'smooth' })
    return
  }
  const top = target.offsetTop - 14
  el.scrollTo({ top, behavior: 'smooth' })
}

/** IntersectionObserver 监听每个用户问题元素，更新 activeUserIdx */
let userQObserver: IntersectionObserver | null = null
function setupUserQObserver() {
  // 断开旧观察器
  if (userQObserver) {
    userQObserver.disconnect()
    userQObserver = null
  }
  const root = scrollContainerRef.value
  if (!root) return
  userQObserver = new IntersectionObserver(
    (entries) => {
      // 选最靠近视区上 1/3 处的元素
      let bestIdx = activeUserIdx.value
      let bestScore = -Infinity
      for (const e of entries) {
        const idxStr = (e.target as HTMLElement).dataset.userIdx
        if (idxStr == null) continue
        const idx = Number(idxStr)
        // 评分：元素顶部距离视区顶部越近分数越高（>0 表示已滚过）
        const top = e.boundingClientRect.top - root.getBoundingClientRect().top
        const score = -Math.abs(top - 40) // 越接近顶部 40px 越好
        if (e.isIntersecting && score > bestScore) {
          bestScore = score
          bestIdx = idx
        }
      }
      if (bestScore > -Infinity) activeUserIdx.value = bestIdx
    },
    { root, rootMargin: '-40px 0px -60% 0px', threshold: [0, 0.1, 0.5, 1] },
  )
  // 注册所有当前用户问题
  for (const idx of questionIdxs.value) {
    const node = root.querySelector<HTMLElement>(`[data-user-idx="${idx}"]`)
    if (node) userQObserver.observe(node)
  }
}

onMounted(() => { nextTick(setupUserQObserver) })
/** 用户问题变化（增/删/切会话）时重建观察器 */
watch(questionIdxs, () => { nextTick(setupUserQObserver) })
onUnmounted(() => { if (userQObserver) userQObserver.disconnect() })
</script>

<template>
  <div class="relative flex-1 min-h-0">
    <!-- 滚动容器（原生滚动条已隐藏，改用自绘 mac 风格滑块） -->
    <div
      ref="scrollContainerRef"
      class="hide-scrollbar-container h-full w-full overflow-y-auto px-4 pt-14 pb-8"
      @scroll="checkStickToBottom"
    >
    <!-- 空态：无消息且无流式 -->
    <div
      v-if="messages.length === 0 && !hasStreaming"
      class="agent-empty-state h-full flex flex-col items-center justify-center text-center px-4"
    >
      <div
        class="w-16 h-16 rounded-full flex items-center justify-center mb-4"
        :class="['theme-primary-bg']"
      >
        <svg class="w-8 h-8 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2z" />
        </svg>
      </div>
      <p class="text-xl font-semibold" :class="['theme-text-main']">你好！我是 ACG Space AI 助手</p>
      <p class="text-sm mt-2" :class="['theme-text-muted']">
        有什么我可以帮你的？可以问我平台玩法、抽赏保底、碎片合成、番剧推荐等问题。
      </p>
      <div class="mt-5 flex flex-wrap justify-center gap-2 max-w-lg">
        <button
          v-for="q in ['抽赏保底机制是什么？', '如何兑换实物奖品？', '推荐几部机战番', '如何合成碎片？']"
          :key="q"
          class="px-3 py-1.5 rounded-full text-xs transition-colors duration-300"
          :class="['theme-card', 'theme-card-hover', 'theme-text-muted']"
          @click="emit('quick-send', q)"
        >
          {{ q }}
        </button>
      </div>
    </div>

    <!-- 搜索无结果 -->
    <div
      v-else-if="searchQuery && searchQuery.trim() && messages.length > 0 && displayMessages.length === 0"
      class="h-full flex flex-col items-center justify-center text-center px-4"
      :class="['theme-text-muted']"
    >
      <p class="text-sm">未找到包含「{{ searchQuery.trim() }}」的消息</p>
      <button class="mt-3 text-xs px-3 py-1.5 rounded-full" :class="['theme-card', 'theme-card-hover']" @click="emit('update:searchQuery', '')">
        清除搜索
      </button>
    </div>

    <!-- 消息列表（<TransitionGroup> 入场丝滑 + 删除/高度变化 FLIP 位移） -->
    <TransitionGroup name="chat-list" tag="div" class="max-w-3xl mx-auto relative">
      <div v-for="msg in displayMessages" :key="msg.id">
        <!-- 用户消息：右对齐，浅色/粉色主题气泡背景，Markdown 不解析避免误渲染 -->
        <div
          v-if="msg.role === 'user'"
          :data-user-idx="userIdxById[msg.id] ?? -1"
          class="flex gap-3 pt-2 pb-3 justify-end items-start"
        >
          <div
            class="max-w-[80%] px-4 py-3 rounded-2xl whitespace-pre-wrap break-words text-sm leading-relaxed"
            :class="['theme-user-bubble']"
          >
            <div v-if="msg.attachmentName" class="flex items-center gap-1.5 mb-1.5 text-xs opacity-80">
              <svg class="w-3.5 h-3.5 flex-shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48" />
              </svg>
              <span class="truncate max-w-[200px]">{{ msg.attachmentName }}</span>
            </div>
            {{ msg.content }}
          </div>
          <div
            class="flex-shrink-0 w-8 h-8 mt-3 rounded-full flex items-center justify-center select-none"
            :class="['theme-card', 'theme-text-muted']"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
        </div>

        <!-- 助手消息：左对齐；头像与气泡顶部对齐；Markdown 渲染 -->
        <div v-else class="flex gap-3 pt-2 pb-3 justify-start items-start">
          <div
            class="flex-shrink-0 w-8 h-8 mt-3 rounded-full flex items-center justify-center text-xs font-bold select-none"
            :class="['theme-primary-bg', 'text-white']"
          >
            AI
          </div>
          <div class="flex flex-col gap-1.5 min-w-0 group">
            <div
              class="max-w-[80%] px-4 py-3 rounded-2xl text-sm leading-relaxed agent-markdown"
              :class="msg.isError ? ['bg-red-500/10', 'border', 'border-red-500/30', 'text-red-400'] : ['theme-card', 'theme-text-main']"
            >
              <div v-if="msg.isError" class="whitespace-pre-wrap">{{ msg.content }}</div>
              <div v-else v-html="renderMarkdown(msg.content)" />
            </div>
            <!-- 操作行：反馈 + 重新生成 -->
            <div class="flex items-center gap-1 pl-1 opacity-0 group-hover:opacity-100 transition-opacity" :class="['theme-text-muted']">
              <button
                class="p-1.5 rounded-lg transition-colors hover:bg-current/10"
                :class="feedbackMap && feedbackMap[msg.id] === 'up' ? 'text-green-500' : ''"
                title="有帮助"
                @click="emit('feedback', msg.id, 'up')"
              >
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 9V5a3 3 0 0 0-3-3l-4 9v11h11.28a2 2 0 0 0 2-1.7l1.38-9a2 2 0 0 0-2-2.3zM7 22H4a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2h3" />
                </svg>
              </button>
              <button
                class="p-1.5 rounded-lg transition-colors hover:bg-current/10"
                :class="feedbackMap && feedbackMap[msg.id] === 'down' ? 'text-red-500' : ''"
                title="没帮助"
                @click="emit('feedback', msg.id, 'down')"
              >
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M10 15v4a3 3 0 0 0 3 3l4-9V2H5.72a2 2 0 0 0-2 1.7l-1.38 9a2 2 0 0 0 2 2.3zm7 0h3a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2h-3" />
                </svg>
              </button>
              <button
                class="p-1.5 rounded-lg transition-colors hover:bg-current/10 flex items-center gap-1"
                title="重新生成"
                @click="emit('regenerate', msg.id)"
              >
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="23 4 23 10 17 10" />
                  <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 思考中：等待首个 token，显示累计秒数（"AI 助手正在思考 · 3s"） -->
      <div v-if="hasStreaming && streamingContent.length === 0" key="thinking" class="flex gap-3 py-3 justify-start items-start">
        <div
          class="flex-shrink-0 w-8 h-8 mt-3 rounded-full flex items-center justify-center text-xs font-bold select-none"
          :class="['theme-primary-bg', 'text-white']"
        >
          AI
        </div>
        <div
          class="px-4 py-3 rounded-2xl flex items-center gap-2"
          :class="['theme-card', 'theme-text-muted']"
        >
          <!-- 工具执行中：显示具体状态（如「正在查询番剧库…」）+ 旋转图标 -->
          <template v-if="toolStatus">
            <span class="animate-spin w-3.5 h-3.5 rounded-full border-2 border-current border-t-transparent" />
            <span class="text-sm">{{ toolStatus }}</span>
          </template>
          <!-- 纯思考中：三点跳动 + 累计秒数 -->
          <template v-else>
            <span class="flex gap-1">
              <span class="w-2 h-2 rounded-full bg-current typing-dot" style="animation-delay: 0ms" />
              <span class="w-2 h-2 rounded-full bg-current typing-dot" style="animation-delay: 150ms" />
              <span class="w-2 h-2 rounded-full bg-current typing-dot" style="animation-delay: 300ms" />
            </span>
            <span class="text-sm"
              >AI 助手正在思考<span v-if="thinkingSeconds > 0"> · {{ thinkingSeconds }}s</span><span v-else>...</span></span
            >
          </template>
        </div>
      </div>

      <!-- 流式生成中（已有 token）：就地渲染 Markdown + 闪烁光标 -->
      <div v-else-if="hasStreaming" key="streaming" class="flex gap-3 pt-2 pb-3 justify-start items-start">
        <div
          class="flex-shrink-0 w-8 h-8 mt-3 rounded-full flex items-center justify-center text-xs font-bold select-none"
          :class="['theme-primary-bg', 'text-white']"
        >
          AI
        </div>
        <div
          class="max-w-[80%] px-4 py-3 rounded-2xl text-sm leading-relaxed agent-markdown"
          :class="['theme-card', 'theme-text-main']"
        >
          <div v-html="renderedStreaming" />
          <span class="typing-cursor" />
        </div>
      </div>
    </TransitionGroup>

    <!-- 锚点：自动滚动至此 -->
    <div ref="anchorRef" class="h-1" />

    <!-- 千问式右侧小横杠 scroll-spy：每个用户问题一个，当前可见的标主色 -->
    <div
      v-if="userQuestions.length >= 2"
      class="agent-scroll-spy"
      :class="['is-scrolling-spy']"
    >
      <button
        v-for="(_, idx) in userQuestions"
        :key="idx"
        type="button"
        class="agent-scroll-spy-bar"
        :class="{ 'is-active': idx === activeUserIdx, ['theme-text-muted']: idx !== activeUserIdx, ['theme-primary-bg']: idx === activeUserIdx }"
        :title="`跳到第 ${idx + 1} 个问题`"
        @click="scrollToUserQuestion(idx)"
      />
    </div>
    </div>

    <!-- 自绘 mac 风格细滑块：静止隐藏，滚动时浮现，可拖拽 -->
    <div
      v-if="canScroll"
      class="agent-custom-scrollbar"
      :class="{ 'is-visible': isScrolling }"
    >
      <div
        class="agent-custom-thumb"
        :style="{ height: thumbHeight + 'px', transform: 'translateY(' + thumbTop + 'px)' }"
        @pointerdown="onThumbPointerDown"
      />
    </div>
  </div>
</template>

<style scoped>
/* 思考中小点跳动 */
.typing-dot {
  display: inline-block;
  animation: typing-bounce 1.2s infinite ease-in-out;
}
@keyframes typing-bounce {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

/* Markdown 渲染样式（:deep 因为 v-html 子元素没有 scoped 属性） */
.agent-markdown :deep(h1),
.agent-markdown :deep(h2),
.agent-markdown :deep(h3),
.agent-markdown :deep(h4) {
  font-weight: 600;
  line-height: 1.3;
}
.agent-markdown :deep(h1) { font-size: 1.2em; margin-top: 0.6em; margin-bottom: 0.4em; }
.agent-markdown :deep(h2) { font-size: 1.1em; margin-top: 0.6em; margin-bottom: 0.4em; }
.agent-markdown :deep(h3) { font-size: 1em;    margin-top: 0.5em; margin-bottom: 0.3em; }
.agent-markdown :deep(h4) { font-size: 0.95em; margin-top: 0.5em; margin-bottom: 0.3em; }

.agent-markdown :deep(p) {
  margin: 0.4em 0;
  line-height: 1.65;
}
.agent-markdown :deep(p:first-child) { margin-top: 0; }
.agent-markdown :deep(p:last-child) { margin-bottom: 0; }

.agent-markdown :deep(ul),
.agent-markdown :deep(ol) {
  margin: 0.4em 0;
  padding-left: 1.4em;
}
.agent-markdown :deep(li) {
  margin: 0.2em 0;
  line-height: 1.6;
}
.agent-markdown :deep(li > p) { margin: 0; }

.agent-markdown :deep(blockquote) {
  border-left: 3px solid currentColor;
  opacity: 0.7;
  padding-left: 0.8em;
  margin: 0.6em 0;
}

.agent-markdown :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 0.6em 0;
  font-size: 0.9em;
}
.agent-markdown :deep(th),
.agent-markdown :deep(td) {
  border: 1px solid var(--theme-border-color, rgba(128, 128, 128, 0.2));
  padding: 8px 12px;
  text-align: left;
  vertical-align: top;
}
.agent-markdown :deep(thead th) {
  background-color: rgba(127, 127, 127, 0.08);
  font-weight: 700;
}

.agent-markdown :deep(a) {
  color: #ec4899; /* 品牌粉，和 AI 头像主色一致 */
  text-decoration: underline;
  text-underline-offset: 2px;
  word-break: break-all;
}
.agent-markdown :deep(a:hover) { opacity: 0.8; }

.agent-markdown :deep(strong) { font-weight: 700; }
.agent-markdown :deep(em) { font-style: italic; }

.agent-markdown :deep(code) {
  background: rgba(127, 127, 127, 0.18);
  padding: 0.1em 0.35em;
  border-radius: 4px;
  font-size: 0.9em;
  font-family: 'Consolas', 'Monaco', 'SF Mono', monospace;
}
.agent-markdown :deep(pre) {
  background: rgba(0, 0, 0, 0.25);
  padding: 0.7em 0.9em;
  border-radius: 8px;
  overflow-x: auto;
  margin: 0.6em 0;
  line-height: 1.5;
}
.agent-markdown :deep(pre code) {
  background: transparent;
  padding: 0;
  font-size: 0.85em;
}

.agent-markdown :deep(hr) {
  border: none;
  border-top: 1px solid rgba(127, 127, 127, 0.3);
  margin: 0.8em 0;
}

/* 番剧封面图：限宽 + 圆角，与气泡融洽 */
.agent-markdown :deep(img) {
  max-width: 140px;
  height: auto;
  border-radius: 8px;
  margin: 0.4em 0;
  display: block;
  background: rgba(127, 127, 127, 0.1); /* 占位灰底，避免图裂时空 */
}

/* ===== 消息列表入场 / 位移动画 (FLIP) ===== */
.chat-list-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}
.chat-list-enter-active {
  transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.chat-list-leave-active {
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: absolute;
  width: 100%;
}
.chat-list-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}
/* 列表高度/顺序变化时的平滑位移（删除消息、流式增长挤压等） */
.chat-list-move {
  transition: transform 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* 流式打字机光标 */
.typing-cursor {
  display: inline-block;
  width: 6px;
  height: 1.2em;
  background-color: var(--theme-primary, #ec4899);
  vertical-align: text-bottom;
  margin-left: 2px;
  animation: blink 1s step-end infinite;
}
@keyframes blink {
  50% { opacity: 0; }
}

/* ===== 千问式右侧小横杠 scroll-spy ===== */
/* 用 fixed 相对视口定位：贴网页右缘 + 垂直居中（二分之一位置），
   不随对话区滚动而上下漂，保证始终在网页右侧固定位置 */
.agent-scroll-spy {
  position: fixed;
  top: 50%;
  right: 8px;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 24px; /* 横杠间距拉开一倍 */
  z-index: 50;
  pointer-events: auto;
}
.agent-scroll-spy-bar {
  width: 16px;
  height: 3px;
  border-radius: 9999px;
  opacity: 0.35;
  transition: opacity 0.2s, width 0.2s, background-color 0.2s;
  cursor: pointer;
  padding: 0;
  background-color: rgba(127, 127, 127, 0.5);
}
.agent-scroll-spy-bar:hover {
  opacity: 0.7;
  width: 20px;
}
.agent-scroll-spy-bar.is-active {
  opacity: 1;
  width: 20px;
}

/* ===== 自绘 mac 风格细滑块（overlay：静止隐藏，滚动浮现，可拖拽） ===== */
.agent-custom-scrollbar {
  position: absolute;
  top: 8px;
  bottom: 8px;
  right: 2px;
  width: 6px;
  z-index: 20;
  opacity: 0;
  transition: opacity 0.25s ease;
  /* 轨道本身不拦截事件，只有 thumb 可点 */
  pointer-events: none;
}
.agent-custom-scrollbar.is-visible {
  opacity: 1;
}
.agent-custom-thumb {
  position: absolute;
  top: 0;
  left: 1px;
  width: 4px;
  border-radius: 9999px;
  /* macOS overlay 灰：随主题自适应（浅色用深灰，深色用浅灰） */
  background-color: rgba(120, 120, 120, 0.5);
  cursor: pointer;
  pointer-events: auto;
  transition: background-color 0.2s ease;
}
.agent-custom-thumb:hover {
  background-color: rgba(120, 120, 120, 0.72);
}
:global(.dark) .agent-custom-thumb,
:global(html.dark) .agent-custom-thumb {
  background-color: rgba(200, 200, 200, 0.5);
}
:global(.dark) .agent-custom-thumb:hover,
:global(html.dark) .agent-custom-thumb:hover {
  background-color: rgba(200, 200, 200, 0.72);
}
</style>
