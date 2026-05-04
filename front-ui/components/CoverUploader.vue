<template>
  <div class="cover-uploader">
    <div v-if="previewUrl && !showCropper" class="cover-preview">
      <img :src="previewUrl" alt="封面预览" />
      <div class="cover-actions">
        <button type="button" @click="editImage">重新编辑</button>
        <button type="button" @click="removeCover">删除</button>
      </div>
    </div>

    <div v-else-if="!showCropper" class="cover-input">
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        @change="handleFileSelect"
        style="display: none"
      />
      <div class="upload-menu">
        <button type="button" @click="showMenu = !showMenu" class="upload-placeholder">
          <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/>
          </svg>
          <span>添加封面</span>
        </button>
        <div v-if="showMenu" class="menu-dropdown">
          <button type="button" @click="handleCopyImage" class="menu-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1" ry="1"/>
            </svg>
            <span>复制图像</span>
          </button>
          <button type="button" @click="triggerFileSelect" class="menu-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
            </svg>
            <span>本地选择</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="showCropper" class="cropper-wrapper">
      <div class="cropper-header">
        <span>裁剪封面图片 (16:9)</span>
        <button type="button" @click="cancelCrop" class="text-slate-400 hover:text-white">取消</button>
      </div>
      <div class="cropper-canvas-container">
        <canvas ref="canvasRef" @mousedown="startDrag" @mousemove="doDrag" @mouseup="endDrag" @wheel="doZoom"></canvas>
      </div>
      <div class="cropper-toolbar">
        <button type="button" @click="rotateLeft" class="toolbar-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/>
          </svg>
          左旋转
        </button>
        <button type="button" @click="rotateRight" class="toolbar-btn">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/>
          </svg>
          右旋转
        </button>
        <input type="range" v-model="zoomLevel" min="50" max="200" class="zoom-slider" />
        <span class="zoom-label">{{ zoomLevel }}%</span>
        <button type="button" @click="confirmCrop" class="confirm-btn">确认</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'

const props = defineProps<{
  modelValue?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const fileInputRef = ref<HTMLInputElement>()
const canvasRef = ref<HTMLCanvasElement>()
const previewUrl = ref(props.modelValue || '')
const showCropper = ref(false)
const showMenu = ref(false)
const zoomLevel = ref(100)

let originalImage: HTMLImageElement | null = null
let imageX = 0
let imageY = 0
let imageScale = 1
let rotation = 0
let isDragging = false
let dragStartX = 0
let dragStartY = 0
let canvasCtx: CanvasRenderingContext2D | null = null
const CANVAS_WIDTH = 640
const CANVAS_HEIGHT = 360

watch(() => props.modelValue, (val) => {
  previewUrl.value = val || ''
})

const triggerFileSelect = () => {
  showMenu.value = false
  fileInputRef.value?.click()
}

const handleFileSelect = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) {
    loadImageForCropper(file)
  }
  input.value = ''
  showMenu.value = false
}

const handlePaste = (event: ClipboardEvent) => {
  showMenu.value = false
  const items = event.clipboardData?.items
  if (!items) return

  for (const item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile()
      if (file) {
        loadImageForCropper(file)
      }
      break
    }
  }
}

const handleCopyImage = async () => {
  showMenu.value = false
  try {
    if (navigator.clipboard && navigator.clipboard.read) {
      const clipboardItems = await navigator.clipboard.read()
      for (const item of clipboardItems) {
        for (const type of item.types) {
          if (type.startsWith('image/')) {
            const blob = await item.getType(type)
            const file = new File([blob], 'clipboard-image.png', { type })
            loadImageForCropper(file)
            return
          }
        }
      }
      alert('剪贴板中没有图片')
    } else if (navigator.clipboard && navigator.clipboard.readText) {
      const text = await navigator.clipboard.readText()
      if (text.startsWith('data:image')) {
        const img = new Image()
        img.onload = () => {
          originalImage = img
          resetImagePosition()
          showCropper.value = true
          setTimeout(drawCanvas, 50)
        }
        img.src = text
      } else {
        alert('剪贴板中没有图片')
      }
    } else {
      alert('您的浏览器不支持复制图像功能，请使用本地选择上传图片')
    }
  } catch (err) {
    console.error('读取剪贴板失败:', err)
    alert('读取剪贴板失败，请确保已授予剪贴板访问权限')
  }
}

const loadImageForCropper = (file: File) => {
  if (file.size > 10 * 1024 * 1024) {
    alert('图片大小不能超过10MB')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    const img = new Image()
    img.onload = () => {
      originalImage = img
      resetImagePosition()
      showCropper.value = true
      setTimeout(drawCanvas, 50)
    }
    img.src = e.target?.result as string
  }
  reader.readAsDataURL(file)
}

const resetImagePosition = () => {
  if (!originalImage) return

  const imgRatio = originalImage.width / originalImage.height
  const canvasRatio = CANVAS_WIDTH / CANVAS_HEIGHT

  if (imgRatio > canvasRatio) {
    imageScale = CANVAS_HEIGHT / originalImage.height
  } else {
    imageScale = CANVAS_WIDTH / originalImage.width
  }

  imageX = (CANVAS_WIDTH - originalImage.width * imageScale) / 2
  imageY = (CANVAS_HEIGHT - originalImage.height * imageScale) / 2
  rotation = 0
  zoomLevel.value = 100
}

const drawCanvas = () => {
  if (!canvasRef.value || !originalImage) return

  const canvas = canvasRef.value
  canvas.width = CANVAS_WIDTH
  canvas.height = CANVAS_HEIGHT
  canvasCtx = canvas.getContext('2d')
  if (!canvasCtx) return

  canvasCtx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)

  canvasCtx.fillStyle = '#1a1a1a'
  canvasCtx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)

  const scale = imageScale * (zoomLevel.value / 100)

  canvasCtx.save()
  canvasCtx.translate(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2)
  canvasCtx.rotate((rotation * Math.PI) / 180)
  canvasCtx.translate(-CANVAS_WIDTH / 2, -CANVAS_HEIGHT / 2)

  canvasCtx.drawImage(
    originalImage,
    imageX + CANVAS_WIDTH / 2 - (originalImage.width * scale) / 2,
    imageY + CANVAS_HEIGHT / 2 - (originalImage.height * scale) / 2,
    originalImage.width * scale,
    originalImage.height * scale
  )

  canvasCtx.restore()

  canvasCtx.strokeStyle = '#409eff'
  canvasCtx.lineWidth = 2
  canvasCtx.setLineDash([5, 5])
  canvasCtx.strokeRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)
}

const startDrag = (e: MouseEvent) => {
  isDragging = true
  dragStartX = e.clientX
  dragStartY = e.clientY
}

const doDrag = (e: MouseEvent) => {
  if (!isDragging) return
  const dx = e.clientX - dragStartX
  const dy = e.clientY - dragStartY
  imageX += dx
  imageY += dy
  dragStartX = e.clientX
  dragStartY = e.clientY
  drawCanvas()
}

const endDrag = () => {
  isDragging = false
}

const doZoom = (e: WheelEvent) => {
  e.preventDefault()
  if (e.deltaY < 0) {
    zoomLevel.value = Math.min(200, zoomLevel.value + 5)
  } else {
    zoomLevel.value = Math.max(50, zoomLevel.value - 5)
  }
  drawCanvas()
}

const rotateLeft = () => {
  rotation = (rotation - 90) % 360
  drawCanvas()
}

const rotateRight = () => {
  rotation = (rotation + 90) % 360
  drawCanvas()
}

const editImage = () => {
  if (previewUrl.value) {
    const img = new Image()
    img.onload = () => {
      originalImage = img
      resetImagePosition()
      showCropper.value = true
      setTimeout(drawCanvas, 50)
    }
    img.src = previewUrl.value
  }
}

const cancelCrop = () => {
  showCropper.value = false
  if (!previewUrl.value) {
    originalImage = null
  }
}

const confirmCrop = () => {
  if (!canvasRef.value || !canvasCtx || !originalImage) return

  const croppedCanvas = document.createElement('canvas')
  croppedCanvas.width = CANVAS_WIDTH
  croppedCanvas.height = CANVAS_HEIGHT
  const ctx = croppedCanvas.getContext('2d')
  if (!ctx) return

  ctx.fillStyle = '#1a1a1a'
  ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)

  const scale = imageScale * (zoomLevel.value / 100)

  ctx.save()
  ctx.translate(CANVAS_WIDTH / 2, CANVAS_HEIGHT / 2)
  ctx.rotate((rotation * Math.PI) / 180)
  ctx.translate(-CANVAS_WIDTH / 2, -CANVAS_HEIGHT / 2)

  ctx.drawImage(
    originalImage,
    imageX + CANVAS_WIDTH / 2 - (originalImage.width * scale) / 2,
    imageY + CANVAS_HEIGHT / 2 - (originalImage.height * scale) / 2,
    originalImage.width * scale,
    originalImage.height * scale
  )

  ctx.restore()

  const dataUrl = croppedCanvas.toDataURL('image/jpeg', 0.9)
  previewUrl.value = dataUrl
  emit('update:modelValue', dataUrl)
  showCropper.value = false
}

const removeCover = () => {
  previewUrl.value = ''
  emit('update:modelValue', '')
  originalImage = null
}

onMounted(() => {
  if (canvasRef.value) {
    canvasCtx = canvasRef.value.getContext('2d')
  }
})
</script>

<style scoped>
.cover-uploader {
  width: 100%;
}

.cover-preview {
  position: relative;
  width: 320px;
  height: 180px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #334155;
}

.cover-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-actions {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  padding: 8px;
  display: flex;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s;
}

.cover-preview:hover .cover-actions {
  opacity: 1;
}

.cover-actions button {
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  cursor: pointer;
  transition: background 0.2s;
}

.cover-actions button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.cover-actions button:last-child {
  background: rgba(239, 68, 68, 0.6);
}

.cover-actions button:last-child:hover {
  background: rgba(239, 68, 68, 0.8);
}

.cover-input {
  width: 320px;
}

.upload-menu {
  position: relative;
}

.upload-placeholder {
  width: 320px;
  height: 180px;
  border: 2px dashed #334155;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: border-color 0.3s;
  color: #64748b;
  background: transparent;
}

.upload-placeholder:hover {
  border-color: #6366f1;
  color: #818cf8;
}

.menu-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 8px;
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 8px;
  overflow: hidden;
  z-index: 10;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
}

.menu-item {
  width: 100%;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #cbd5e1;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
  text-align: left;
}

.menu-item:hover {
  background: #334155;
  color: white;
}

.cropper-wrapper {
  background: #1e293b;
  border-radius: 8px;
  padding: 16px;
}

.cropper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  color: #fff;
}

.cropper-canvas-container {
  background: #0f172a;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
}

.cropper-canvas-container canvas {
  cursor: move;
  max-width: 100%;
}

.cropper-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  color: #fff;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: #334155;
  border: none;
  border-radius: 6px;
  color: #cbd5e1;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}

.toolbar-btn:hover {
  background: #475569;
  color: white;
}

.zoom-slider {
  width: 120px;
  height: 4px;
  background: #334155;
  border-radius: 2px;
  appearance: none;
  cursor: pointer;
}

.zoom-slider::-webkit-slider-thumb {
  appearance: none;
  width: 14px;
  height: 14px;
  background: #6366f1;
  border-radius: 50%;
  cursor: pointer;
}

.zoom-label {
  font-size: 12px;
  color: #94a3b8;
  width: 45px;
}

.confirm-btn {
  margin-left: auto;
  padding: 8px 20px;
  background: #6366f1;
  border: none;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}

.confirm-btn:hover {
  background: #4f46e5;
}
</style>