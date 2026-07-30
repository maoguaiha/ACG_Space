<script setup lang="ts">
/**
 * AI 设置对话框（三主题适配）。
 *
 * 让用户在对话前选择模型与采样温度。设置存于前端 localStorage，
 * 发送时随请求透传（→ 后端 → python-agent），不落库。
 *
 * 用法：
 *   <AgentSettingsDialog
 *     v-model:open="settingsOpen"
 *     :models="availableModels"
 *     :current-model="agentSettings.model"
 *     :current-temperature="agentSettings.temperature"
 *     @confirm="handleSettingsConfirm"
 *   />
 */
import { ref, watch } from 'vue'

const props = defineProps<{
  open: boolean
  /** 可选模型列表（由后端/配置决定，当前仅 LongCat-2.0） */
  models?: string[]
  currentModel: string
  /** 采样温度 0~1 */
  currentTemperature: number
}>()

const emit = defineEmits<{
  'update:open': [v: boolean]
  confirm: [settings: { model: string; temperature: number }]
}>()

const selectedModel = ref(props.currentModel)
const temperature = ref(props.currentTemperature)
const modelList = props.models && props.models.length ? props.models : ['LongCat-2.0']

watch(
  () => props.open,
  (o) => {
    if (o) {
      selectedModel.value = props.currentModel
      temperature.value = props.currentTemperature
    }
  },
  { immediate: true },
)

function close() {
  emit('update:open', false)
}

function onConfirm() {
  emit('confirm', {
    model: selectedModel.value,
    temperature: Math.min(1, Math.max(0, temperature.value)),
  })
  emit('update:open', false)
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape') close()
}
</script>

<template>
  <Teleport to="body">
    <Transition name="agent-dialog">
      <div
        v-if="open"
        class="agent-dialog-backdrop"
        @click.self="close"
        @keydown="onKeydown"
        tabindex="-1"
      >
        <div class="agent-dialog-panel" role="dialog" aria-modal="true" aria-label="AI 设置">
          <!-- 标题栏 -->
          <div
            class="flex items-center justify-between px-5 py-3 border-b"
            :class="['agent-dialog-header', 'theme-border']"
          >
            <h2 class="text-base font-semibold" :class="['theme-text-main']">AI 设置</h2>
            <button class="agent-dialog-close" :class="['theme-text-muted']" aria-label="关闭" @click="close">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>
          </div>

          <!-- 设置区 -->
          <div class="px-5 py-4 space-y-5">
            <!-- 模型选择 -->
            <div>
              <label class="block text-sm font-medium mb-2" :class="['theme-text-main']">模型</label>
              <div class="relative">
                <select
                  v-model="selectedModel"
                  class="agent-dialog-input w-full appearance-none pr-9 cursor-pointer"
                  :class="['theme-text-main', 'theme-card']"
                >
                  <option v-for="m in modelList" :key="m" :value="m">{{ m }}</option>
                </select>
                <svg
                  class="w-4 h-4 absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none"
                  :class="['theme-text-muted']"
                  viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"
                >
                  <polyline points="6 9 12 15 18 9" />
                </svg>
              </div>
              <p class="text-xs mt-1.5" :class="['theme-text-muted']">
                当前仅 LongCat-2.0 已配置；接入其他模型需在 python-agent 配置对应 API Key。
              </p>
            </div>

            <!-- 温度滑块 -->
            <div>
              <div class="flex items-center justify-between mb-2">
                <label class="text-sm font-medium" :class="['theme-text-main']">采样温度</label>
                <span class="text-xs font-mono px-2 py-0.5 rounded" :class="['theme-card', 'theme-text-muted']">
                  {{ temperature.toFixed(2) }}
                </span>
              </div>
              <input
                v-model.number="temperature"
                type="range"
                min="0"
                max="1"
                step="0.05"
                class="agent-temp-slider w-full"
              />
              <div class="flex justify-between text-xs mt-1" :class="['theme-text-muted']">
                <span>精确 (0)</span>
                <span> creative (1)</span>
              </div>
            </div>
          </div>

          <!-- 底部按钮 -->
          <div
            class="flex items-center justify-end gap-2 px-5 py-3 border-t"
            :class="['agent-dialog-footer', 'theme-border']"
          >
            <button class="agent-dialog-btn agent-dialog-btn-cancel" @click="close">取消</button>
            <button class="agent-dialog-btn agent-dialog-btn-primary" @click="onConfirm">保存</button>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.agent-dialog-enter-active,
.agent-dialog-leave-active {
  transition: opacity 0.18s ease;
}
.agent-dialog-enter-from,
.agent-dialog-leave-to {
  opacity: 0;
}

/* 温度滑块主题色（品牌粉 #ec4899） */
.agent-temp-slider {
  -webkit-appearance: none;
  appearance: none;
  height: 6px;
  border-radius: 9999px;
  background: rgba(127, 127, 127, 0.25);
  outline: none;
}
.agent-temp-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 16px;
  height: 16px;
  border-radius: 9999px;
  background: #ec4899;
  cursor: pointer;
  box-shadow: 0 2px 6px rgba(236, 72, 153, 0.4);
}
.agent-temp-slider::-moz-range-thumb {
  width: 16px;
  height: 16px;
  border: none;
  border-radius: 9999px;
  background: #ec4899;
  cursor: pointer;
}
</style>
