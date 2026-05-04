<template>
  <div class="rich-editor">
    <div class="editor-toolbar">
      <div class="toolbar-left">
        <el-button size="small" @click="toggleMode">
          {{ isMarkdown ? '富文本' : 'Markdown' }}
        </el-button>
        <el-divider direction="vertical" />
        <el-button size="small" @click="execCommand('bold')" title="加粗">
          <strong>B</strong>
        </el-button>
        <el-button size="small" @click="execCommand('italic')" title="斜体">
          <em>I</em>
        </el-button>
        <el-button size="small" @click="execCommand('underline')" title="下划线">
          <u>U</u>
        </el-button>
        <el-divider direction="vertical" />
        <el-button size="small" @click="execCommand('insertUnorderedList')" title="无序列表">
          <el-icon><List /></el-icon>
        </el-button>
        <el-button size="small" @click="execCommand('insertOrderedList')" title="有序列表">
          <el-icon><List /></el-icon>
        </el-button>
        <el-button size="small" @click="execCommand('formatBlock', 'blockquote')" title="引用">
          <el-icon><MessageSquare /></el-icon>
        </el-button>
        <el-button size="small" @click="execCommand('formatBlock', 'h2')" title="标题">
          <el-icon><Type /></el-icon>
        </el-button>
        <el-divider direction="vertical" />
        <el-button size="small" @click="execCommand('createLink')" title="插入链接">
          <el-icon><Link /></el-icon>
        </el-button>
        <el-button size="small" @click="triggerImageUpload" title="插入图片">
          <el-icon><Image /></el-icon>
        </el-button>
      </div>
      <div class="toolbar-right">
        <el-button size="small" type="info" @click="triggerFileUpload">
          <el-icon><FileText /></el-icon>
          解析文件
        </el-button>
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
        <el-input
          ref="textareaRef"
          v-model="markdownContent"
          @input="handleInput"
          type="textarea"
          :rows="15"
          :placeholder="placeholder"
          class="markdown-textarea"
        />
      </div>
      <div class="editor-preview markdown-body" v-html="renderedHtml"></div>
    </div>
    <div v-else class="editor-container">
      <div
        ref="editorRef"
        @input="handleInput"
        @paste="handlePaste"
        contenteditable="true"
        :placeholder="placeholder"
        class="contenteditable-editor"
      ></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  modelValue?: string
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const isMarkdown = ref(false)
const editorRef = ref<HTMLDivElement>()
const textareaRef = ref()
const imageInputRef = ref()
const fileInputRef = ref()
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
    const textarea = textareaRef.value?.textArea
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
        const textarea = textareaRef.value?.textArea
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
    ElMessage.warning('图片大小不能超过5MB')
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
.rich-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  flex-wrap: wrap;
  gap: 8px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.editor-container {
  min-height: 400px;
  display: flex;
}

.editor-wrapper {
  flex: 1;
}

.markdown-textarea {
  width: 100%;
}

.markdown-textarea :deep(.el-textarea__inner) {
  font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  background: #1e1e1e;
  color: #d4d4d4;
  border: none;
  border-radius: 0;
  resize: none;
}

.editor-preview {
  flex: 1;
  padding: 16px;
  background: #ffffff;
  border-left: 1px solid #dcdfe6;
  color: #333;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.6;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  color: #303133;
  margin: 16px 0 8px;
}

.markdown-body :deep(blockquote) {
  border-left: 3px solid #409eff;
  padding-left: 12px;
  color: #909399;
  margin: 8px 0;
}

.markdown-body :deep(code) {
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
}

.markdown-body :deep(pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
}

.contenteditable-editor {
  flex: 1;
  min-height: 400px;
  padding: 16px;
  background: #ffffff;
  color: #333;
  font-size: 14px;
  line-height: 1.6;
  outline: none;
  overflow-y: auto;
}

.contenteditable-editor:empty:before {
  content: attr(placeholder);
  color: #c0c4cc;
  pointer-events: none;
}

.contenteditable-editor :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 8px 0;
}

.contenteditable-editor :deep(a) {
  color: #409eff;
  text-decoration: underline;
}

.contenteditable-editor :deep(blockquote) {
  border-left: 3px solid #409eff;
  padding-left: 12px;
  color: #909399;
  margin: 8px 0;
}

.contenteditable-editor :deep(ul),
.contenteditable-editor :deep(ol) {
  padding-left: 24px;
  margin: 8px 0;
}
</style>