<template>
  <div class="tag-selector">
    <div v-if="selectedTags.length > 0" class="selected-tags mb-2 flex flex-wrap gap-2">
      <el-tag
        v-for="tag in selectedTags"
        :key="tag"
        closable
        @close="removeTag(tag)"
        type="info"
        class="custom-tag"
      >
        {{ tag }}
      </el-tag>
    </div>

    <div v-if="showCustomInput" class="custom-input-area mb-2 p-3 bg-gray-50 rounded-lg">
      <el-input
        v-model="customTagInput"
        @keyup.enter.native="confirmCustomTag"
        placeholder="输入自定义标签名称"
        size="small"
        maxlength="20"
        ref="customInputRef"
        class="mb-2"
      >
        <template #append>
          <el-button @click="confirmCustomTag" :disabled="!customTagInput.trim()">添加</el-button>
        </template>
      </el-input>
      <span class="text-xs text-gray-400">标签名称最多20个字符</span>
      <el-button text type="danger" size="small" @click="cancelCustomTag" class="ml-2">取消</el-button>
    </div>

    <div class="preset-tags">
      <span class="text-xs text-gray-500 mr-2">预设标签：</span>
      <div class="flex flex-wrap gap-1 mt-1">
        <el-tag
          v-for="tag in presetTags.filter(t => !selectedTags.includes(t))"
          :key="tag"
          class="preset-tag cursor-pointer"
          @click="selectSuggestion(tag)"
        >
          + {{ tag }}
        </el-tag>
        <el-tag
          class="cursor-pointer add-other-tag"
          @click="showCustomInput = true"
        >
          + 其他
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'

const props = defineProps<{
  modelValue?: string
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
const customInputRef = ref()

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

<style scoped>
.custom-tag {
  background-color: rgba(99, 102, 241, 0.1);
  border-color: rgba(99, 102, 241, 0.2);
  color: #6366f1;
}

.preset-tag {
  background-color: #f1f5f9;
  border-color: #e2e8f0;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.preset-tag:hover {
  background-color: #e2e8f0;
  color: #475569;
}

.add-other-tag {
  background-color: rgba(245, 158, 11, 0.1);
  border-color: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  cursor: pointer;
  transition: all 0.2s;
}

.add-other-tag:hover {
  background-color: rgba(245, 158, 11, 0.2);
  color: #d97706;
}
</style>