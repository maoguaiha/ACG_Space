<template>
  <div class="tag-selector">
    <div v-if="selectedTags.length > 0" class="selected-tags mb-3 flex flex-wrap gap-2">
      <span
        v-for="tag in selectedTags"
        :key="tag"
        class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm"
        :class="['theme-selected-tag']"
      >
        {{ tag }}
        <button @click="removeTag(tag)" type="button" class="transition-colors" :class="['theme-remove-tag-btn']">
          <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </span>
    </div>

    <div v-if="showCustomInput" class="custom-input-area mb-3 p-3 rounded-lg border" :class="['theme-custom-input-area']">
      <div class="flex items-center gap-2 mb-2">
        <input
          v-model="customTagInput"
          @keydown.enter.prevent="confirmCustomTag"
          placeholder="输入自定义标签名称"
          class="flex-1 p-2 border rounded-lg text-sm"
          :class="['theme-custom-tag-input']"
          maxlength="20"
          ref="customInputRef"
        />
        <button
          @click="confirmCustomTag"
          type="button"
          :disabled="!customTagInput.trim()"
          class="px-4 py-2 text-sm rounded-lg transition-colors"
          :class="['theme-add-tag-btn']"
        >
          添加
        </button>
        <button
          @click="cancelCustomTag"
          type="button"
          class="px-4 py-2 text-sm rounded-lg transition-colors"
          :class="['theme-cancel-tag-btn']"
        >
          取消
        </button>
      </div>
      <p class="text-xs" :class="['theme-text-muted']">标签名称最多20个字符</p>
    </div>

    <div v-if="showPresetTags" class="preset-tags">
      <span class="text-xs mb-2 block" :class="['theme-text-muted']">预设标签：</span>
      <div class="flex flex-wrap gap-2">
        <button
          v-for="tag in presetTags.filter(t => !selectedTags.includes(t))"
          :key="tag"
          @click="selectSuggestion(tag)"
          type="button"
          class="px-3 py-1.5 text-xs rounded-full transition-colors"
          :class="['theme-preset-tag']"
        >
          + {{ tag }}
        </button>
        <button
          @click="showCustomInput = true"
          type="button"
          class="px-3 py-1.5 text-xs rounded-full transition-colors"
          :class="['theme-other-tag-btn']"
        >
          + 其他
        </button>
      </div>
    </div>

    <div v-else class="custom-only">
      <button
        @click="showCustomInput = true"
        type="button"
        class="px-3 py-1.5 text-xs rounded-full transition-colors"
        :class="['theme-add-custom-tag-btn']"
      >
        + 添加自定义标签
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'

const props = defineProps<{
  modelValue?: string
  showPresetTags?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const presetTags = [
  '新番推荐', '热门资讯', '深度解析', '番剧点评', '游戏资讯',
  '周边评测', '同人创作', 'COSPLAY', '声优访谈', '音乐欣赏',
  '壁纸分享', '经典回顾', '追番列表', '漫展情报', '手办模型'
]

const customTagInput = ref('')
const showCustomInput = ref(false)
const customInputRef = ref<HTMLInputElement>()

const selectedTags = computed<string[]>(() => {
  if (!props.modelValue) return []
  return props.modelValue.split(',').filter(t => t.trim())
})

watch(showCustomInput, (val) => {
  if (val) {
    nextTick(() => {
      customInputRef.value?.focus()
    })
  }
})

function addTag(tag: string) {
  const trimmedTag = tag.trim()
  if (trimmedTag && !selectedTags.value.includes(trimmedTag)) {
    const newTags = [...selectedTags.value, trimmedTag]
    emit('update:modelValue', newTags.join(','))
  }
}

function selectSuggestion(tag: string) {
  addTag(tag)
}

function confirmCustomTag() {
  if (customTagInput.value.trim()) {
    addTag(customTagInput.value.trim())
    customTagInput.value = ''
    showCustomInput.value = false
  }
}

function cancelCustomTag() {
  customTagInput.value = ''
  showCustomInput.value = false
}

function removeTag(tag: string) {
  const newTags = selectedTags.value.filter(t => t !== tag)
  emit('update:modelValue', newTags.join(','))
}
</script>