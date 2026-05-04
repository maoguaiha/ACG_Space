<template>
  <div class="rich-editor" :class="['theme-rich-editor']">
    <div class="editor-toolbar" :class="['theme-editor-toolbar']">
      <div class="toolbar-left">
        <button type="button" @click="toggleMode" class="mode-switch" :class="['theme-mode-switch']">
          {{ isMarkdown ? '富文本' : 'Markdown' }}
        </button>
        <div class="toolbar-divider" :class="['theme-toolbar-divider']"></div>
        <button type="button" @click="execCommand('bold')" class="toolbar-btn" :class="['theme-toolbar-btn']" title="加粗">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 4h8a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/><path d="M6 12h9a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/></svg>
        </button>
        <button type="button" @click="execCommand('italic')" class="toolbar-btn" :class="['theme-toolbar-btn']" title="斜体">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="4" x2="10" y2="4"/><line x1="14" y1="20" x2="5" y2="20"/><line x1="15" y1="4" x2="9" y2="20"/></svg>
        </button>
        <button type="button" @click="execCommand('underline')" class="toolbar-btn" :class="['theme-toolbar-btn']" title="下划线">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 3v7a6 6 0 0 0 6 6 6 6 0 0 0 6-6V3"/><line x1="4" y1="21" x2="20" y2="21"/></svg>
        </button>
        <div class="toolbar-divider" :class="['theme-toolbar-divider']"></div>
        <button type="button" @click="execCommand('insertUnorderedList')" class="toolbar-btn" :class="['theme-toolbar-btn']" title="无序列表">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/><line x1="8" y1="18" x2="21" y2="18"/><circle cx="3" cy="6" r="1" fill="currentColor"/><circle cx="3" cy="12" r="1" fill="currentColor"/><circle cx="3" cy="18" r="1" fill="currentColor"/></svg>
        </button>
        <button type="button" @click="execCommand('insertOrderedList')" class="toolbar-btn" :class="['theme-toolbar-btn']" title="有序列表">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="10" y1="6" x2="21" y2="6"/><line x1="10" y1="12" x2="21" y2="12"/><line x1="10" y1="18" x2="21" y2="18"/><path d="M4 6h1v4"/><path d="M4 10h2"/><path d="M6 18H4c0-1 2-2 2-3s-1-1.5-2-1"/></svg>
        </button>
        <button type="button" @click="execCommand('formatBlock', 'blockquote')" class="toolbar-btn" :class="['theme-toolbar-btn']" title="引用">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 21c3 0 7-1 7-8V5c0-1.25-.756-2.017-2-2H4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2 1 0 1 0 1 1v1c0 1-1 2-2 2s-1 .008-1 1.031V21z"/><path d="M15 21c3 0 7-1 7-8V5c0-1.25-.757-2.017-2-2h-4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2h.75c0 2.25.25 4-2.75 4v4z"/></svg>
        </button>
        <button type="button" @click="execCommand('formatBlock', 'h2')" class="toolbar-btn" :class="['theme-toolbar-btn']" title="标题">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 12h8"/><path d="M4 18V6"/><path d="M12 18V6"/><path d="M17 12l3-2v8"/></svg>
        </button>
        <div class="toolbar-divider" :class="['theme-toolbar-divider']"></div>
        <button type="button" @click="execCommand('createLink')" class="toolbar-btn" :class="['theme-toolbar-btn']" title="插入链接">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
        </button>
        <button type="button" @click="triggerImageUpload" class="toolbar-btn" :class="['theme-toolbar-btn']" title="插入图片">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
        </button>
      </div>
      <div class="toolbar-right">
        <button type="button" @click="triggerFileUpload" class="file-upload-btn" :class="['theme-file-upload-btn']">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
          解析文件
        </button>
      </div>
    </div>
    <input
      ref="imageInputRef"
      type="file"
      accept="image/*"
      @change="handleImageUpload"
      style="display: none"
    />
    <input
      ref="fileInputRef"
      type="file"
      accept=".txt,.md"
      @change="handleFileUpload"
      style="display: none"
    />
    <div v-if="isMarkdown" class="editor-container">
      <div class="editor-wrapper">
        <textarea
          ref="textareaRef"
          v-model="markdownContent"
          @input="handleInput"
          @keydown.tab="handleTab"
          :placeholder="placeholder"
          class="markdown-textarea" :class="['theme-markdown-textarea']"
        ></textarea>
      </div>
      <div class="editor-preview markdown-body" v-html="renderedHtml" :class="['theme-editor-preview']"></div>
    </div>
    <div v-else class="editor-container">
      <div
        ref="editorRef"
        @input="handleInput"
        @paste="handlePaste"
        contenteditable="true"
        :placeholder="placeholder"
        class="contenteditable-editor" :class="['theme-contenteditable-editor']"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue'

const props = defineProps<{
  modelValue?: string
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const isMarkdown = ref(false)
const editorRef = ref<HTMLDivElement>()
const textareaRef = ref<HTMLTextAreaElement>()
const imageInputRef = ref<HTMLInputElement>()
const fileInputRef = ref<HTMLInputElement>()
const markdownContent = ref('')

watch(() => props.modelValue, (val) => {
  if (isMarkdown.value) {
    markdownContent.value = val || ''
  } else {
    if (editorRef.value && editorRef.value.innerHTML !== (val || '')) {
      editorRef.value.innerHTML = val || ''
    }
  }
}, { immediate: true })

const renderedHtml = computed(() => {
  if (!markdownContent.value) return ''
  return renderMarkdown(markdownContent.value)
})

function renderMarkdown(text: string): string {
  let html = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    .replace(/__([^_]+)__/g, '<strong>$1</strong>')
    .replace(/_([^_]+)_/g, '<em>$1</em>')
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/^\> (.+)$/gm, '<blockquote>$1</blockquote>')
    .replace(/^\- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
    .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
    .replace(/\n/g, '<br>')
  return html
}

function toggleMode() {
  isMarkdown.value = !isMarkdown.value
  nextTick(() => {
    if (isMarkdown.value) {
      markdownContent.value = props.modelValue || ''
    } else {
      if (editorRef.value) {
        editorRef.value.innerHTML = props.modelValue || ''
      }
    }
  })
}

function execCommand(command: string, value?: string) {
  if (isMarkdown.value) {
    const textarea = textareaRef.value
    if (!textarea) return
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const selectedText = markdownContent.value.substring(start, end)
    let replacement = ''
    switch (command) {
      case 'bold':
        replacement = `**${selectedText || '粗体文本'}**`
        break
      case 'italic':
        replacement = `*${selectedText || '斜体文本'}*`
        break
      case 'underline':
        replacement = `__${selectedText || '下划线文本'}__`
        break
      case 'insertUnorderedList':
        replacement = `\n- ${selectedText || '列表项'}`
        break
      case 'insertOrderedList':
        replacement = `\n1. ${selectedText || '列表项'}`
        break
      case 'blockquote':
        replacement = `\n> ${selectedText || '引用文本'}`
        break
      case 'formatBlock':
        if (value === 'h2') {
          replacement = `\n## ${selectedText || '标题'}`
        }
        break
      case 'createLink':
        const url = prompt('请输入链接地址：')
        if (url) {
          replacement = `[${selectedText || '链接文本'}](${url})`
        }
        break
      default:
        replacement = selectedText
    }
    markdownContent.value = markdownContent.value.substring(0, start) + replacement + markdownContent.value.substring(end)
    emit('update:modelValue', markdownContent.value)
  } else {
    document.execCommand(command, false, value)
    editorRef.value?.focus()
    emit('update:modelValue', editorRef.value?.innerHTML || '')
  }
}

function handleInput() {
  if (isMarkdown.value) {
    emit('update:modelValue', markdownContent.value)
  } else {
    emit('update:modelValue', editorRef.value?.innerHTML || '')
  }
}

function handleTab(e: KeyboardEvent) {
  e.preventDefault()
  const textarea = textareaRef.value
  if (!textarea) return
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  markdownContent.value = markdownContent.value.substring(0, start) + '  ' + markdownContent.value.substring(end)
  nextTick(() => {
    textarea.selectionStart = textarea.selectionEnd = start + 2
  })
}

function handlePaste(e: ClipboardEvent) {
  if (isMarkdown.value) return
  const items = e.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      e.preventDefault()
      const file = item.getAsFile()
      if (file) {
        uploadImage(file)
      }
      break
    }
  }
}

function triggerImageUpload() {
  imageInputRef.value?.click()
}

function handleImageUpload(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) {
    if (isMarkdown.value) {
      const reader = new FileReader()
      reader.onload = (evt) => {
        const imgMarkdown = `![${file.name}](${evt.target?.result})`
        const textarea = textareaRef.value
        if (textarea) {
          const start = textarea.selectionStart
          markdownContent.value = markdownContent.value.substring(0, start) + imgMarkdown + markdownContent.value.substring(start)
        } else {
          markdownContent.value += imgMarkdown
        }
        emit('update:modelValue', markdownContent.value)
      }
      reader.readAsDataURL(file)
    } else {
      uploadImage(file)
    }
  }
  input.value = ''
}

function uploadImage(file: File) {
  if (file.size > 5 * 1024 * 1024) {
    alert('图片大小不能超过5MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    const dataUrl = e.target?.result as string
    const img = document.createElement('img')
    img.src = dataUrl
    img.style.maxWidth = '100%'
    img.onload = () => {
      document.execCommand('insertHTML', false, `<img src="${dataUrl}" style="max-width:100%;border-radius:8px;" />`)
      editorRef.value?.focus()
      emit('update:modelValue', editorRef.value?.innerHTML || '')
    }
  }
  reader.readAsDataURL(file)
}

function triggerFileUpload() {
  fileInputRef.value?.click()
}

function handleFileUpload(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (evt) => {
      const content = evt.target?.result as string
      if (isMarkdown.value) {
        markdownContent.value += '\n\n' + content
        emit('update:modelValue', markdownContent.value)
      } else {
        const html = renderMarkdown(content)
        document.execCommand('insertHTML', false, html)
        editorRef.value?.focus()
        emit('update:modelValue', editorRef.value?.innerHTML || '')
      }
    }
    reader.readAsText(file)
  }
  input.value = ''
}
</script>

<style scoped>
.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.mode-switch {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.toolbar-divider {
  width: 1px;
  height: 20px;
  margin: 0 4px;
}

.toolbar-btn {
  padding: 6px 8px;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-upload-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.editor-container {
  min-height: 400px;
}

.editor-wrapper {
  display: flex;
  height: 400px;
}

.markdown-textarea {
  flex: 1;
  width: 100%;
  height: 100%;
  padding: 16px;
  border: none;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  line-height: 1.6;
  resize: none;
  outline: none;
}

.editor-preview {
  flex: 1;
  padding: 16px;
  border-left: 1px solid;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 16px 0 8px;
}

.markdown-body :deep(blockquote) {
  border-left: 3px solid;
  padding-left: 12px;
  margin: 8px 0;
}

.markdown-body :deep(code) {
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
}

.markdown-body :deep(pre) {
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
}

.contenteditable-editor {
  min-height: 400px;
  padding: 16px;
  font-size: 14px;
  line-height: 1.6;
  outline: none;
  overflow-y: auto;
}

.contenteditable-editor:empty:before {
  content: attr(placeholder);
  pointer-events: none;
}

.contenteditable-editor :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}

.contenteditable-editor :deep(a) {
  text-decoration: underline;
}

.contenteditable-editor :deep(blockquote) {
  border-left: 3px solid;
  padding-left: 12px;
  margin: 8px 0;
}

.contenteditable-editor :deep(ul),
.contenteditable-editor :deep(ol) {
  padding-left: 24px;
  margin: 8px 0;
}
</style>