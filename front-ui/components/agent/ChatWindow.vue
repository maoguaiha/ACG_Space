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
import { ref, watch, computed, nextTick, onUnmounted } from 'vue'
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
}>()

const emit = defineEmits<{
  'quick-send': [q: string]
}>()

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
}

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
</script>

<template>
  <div
    ref="scrollContainerRef"
    class="hide-scrollbar-container flex-1 overflow-y-auto px-4 pt-14 pb-8"
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

    <!-- 消息列表（<TransitionGroup> 入场丝滑 + 删除/高度变化 FLIP 位移） -->
    <TransitionGroup name="chat-list" tag="div" class="max-w-3xl mx-auto relative">
      <div v-for="msg in messages" :key="msg.id">
        <!-- 用户消息：右对齐，浅色/粉色主题气泡背景，Markdown 不解析避免误渲染 -->
        <div v-if="msg.role === 'user'" class="flex gap-3 pt-2 pb-3 justify-end items-start">
          <div
            class="max-w-[80%] px-4 py-3 rounded-2xl whitespace-pre-wrap break-words text-sm leading-relaxed"
            :class="['theme-user-bubble']"
          >
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
          <div
            class="max-w-[80%] px-4 py-3 rounded-2xl text-sm leading-relaxed agent-markdown"
            :class="msg.isError ? ['bg-red-500/10', 'border', 'border-red-500/30', 'text-red-400'] : ['theme-card', 'theme-text-main']"
          >
            <div v-if="msg.isError" class="whitespace-pre-wrap">{{ msg.content }}</div>
            <div v-else v-html="renderMarkdown(msg.content)" />
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
</style>
