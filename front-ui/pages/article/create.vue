<template>
  <div class="max-w-3xl mx-auto p-4">
    <div class="flex items-center gap-4 mb-6">
      <NuxtLink to="/article" class="p-2 rounded-lg transition-colors" :class="['theme-btn-back']">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
      </NuxtLink>
      <h1 class="text-2xl font-bold" :class="['theme-text-main']">写文章</h1>
    </div>

    <form @submit.prevent="handleSubmit">
      <div class="mb-4">
        <label class="block text-sm mb-2" :class="['theme-text-muted']">文章标题</label>
        <input v-model="form.title" placeholder="请输入标题" class="w-full p-3 border rounded-lg" :class="['theme-input-field']" />
      </div>
      <div class="mb-4">
        <label class="block text-sm mb-2" :class="['theme-text-muted']">文章分类</label>
        <select v-model="form.category" class="w-full p-3 border rounded-lg" :class="['theme-input-field']">
          <option value="业界资讯">业界资讯</option>
          <option value="深度解析">深度解析</option>
          <option value="新番导视">新番导视</option>
          <option value="周边评测">周边评测</option>
        </select>
      </div>
      <div class="mb-4">
        <label class="block text-sm mb-2" :class="['theme-text-muted']">文章内容</label>
        <RichTextEditor v-model="form.content" placeholder="文章内容（Markdown 支持）" class="w-full border rounded-lg" :class="['theme-input-field']" />
      </div>
      <div class="mb-4">
        <label class="block text-sm mb-2" :class="['theme-text-muted']">摘要（可选）</label>
        <input v-model="form.summary" placeholder="文章摘要" class="w-full p-3 border rounded-lg" :class="['theme-input-field']" />
      </div>
      <div class="mb-4">
        <label class="block text-sm mb-2" :class="['theme-text-muted']">封面图片</label>
        <CoverUploader v-model="form.coverUrl" />
      </div>
      <div class="mb-4">
        <label class="block text-sm mb-2" :class="['theme-text-muted']">标签（可选，最多5个）</label>
        <TagSelector v-model="form.tags" :show-preset-tags="true" />
      </div>
      <div class="flex gap-3">
        <button type="button" @click="handleSubmit(0)" :disabled="submitting" class="px-5 py-2.5 disabled:opacity-50 rounded-lg transition-colors" :class="['theme-btn-secondary']">保存为草稿</button>
        <button type="button" @click="handleSubmit(3)" :disabled="submitting" class="px-5 py-2.5 disabled:opacity-50 rounded-lg transition-colors" :class="['theme-btn-submit']">提交审核</button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createArticle } from '~/composables/useApi'

const router = useRouter()
const submitting = ref(false)
const form = reactive({
  title: '',
  summary: '',
  content: '',
  coverUrl: '',
  category: '业界资讯',
  tags: '',
  status: 0
})

async function handleSubmit(status: number) {
  if (!form.title.trim()) {
    alert('请输入标题')
    return
  }
  if (!form.content.trim()) {
    alert('请输入内容')
    return
  }

  submitting.value = true
  try {
    await createArticle({
      title: form.title.trim(),
      summary: form.summary.trim(),
      content: form.content.trim(),
      coverUrl: form.coverUrl.trim(),
      category: form.category,
      tags: form.tags,
      status: status
    })
    alert(status === 3 ? '提交审核成功！等待管理员审核' : '保存草稿成功！')
    router.push('/article')
  } catch (err) {
    console.error('提交失败', err)
    alert('提交失败，请稍后再试')
  } finally {
    submitting.value = false
  }
}
</script>
