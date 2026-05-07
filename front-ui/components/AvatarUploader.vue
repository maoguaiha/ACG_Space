<template>
  <div class="avatar-uploader">
    <div v-if="previewUrl && !showCropper" class="avatar-preview">
      <img :src="previewUrl" alt="头像预览" class="avatar-img" />
      <div class="avatar-overlay">
        <button type="button" @click="editImage">重新编辑</button>
        <button type="button" @click="removeAvatar" class="remove-btn">删除</button>
      </div>
    </div>

    <div v-else-if="!showCropper" class="avatar-input">
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        @change="handleFileSelect"
        style="display: none"
      />
      <div class="upload-menu">
        <button type="button" @click="showMenu = !showMenu" class="upload-placeholder">
          <div class="avatar-circle">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/>
            </svg>
          </div>
          <span>修改头像</span>
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
        <span>裁剪头像图片 (1:1)</span>
        <button type="button" @click="cancelCrop" class="text-slate-400 hover:text-white">取消</button>
      </div>
      <div class="cropper-canvas-container">
        <div class="circle-mask">
          <canvas ref="canvasRef" @mousedown="startDrag" @mousemove="doDrag" @mouseup="endDrag" @wheel="doZoom"></canvas>
        </div>
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
import { ref, watch } from 'vue'

const props = defineProps<{
  modelValue?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'cropper-show': []
  'cropper-hide': []
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
const CANVAS_SIZE = 400

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
          emit('cropper-show')
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
      emit('cropper-show')
      setTimeout(drawCanvas, 50)
    }
    img.src = e.target?.result as string
  }
  reader.readAsDataURL(file)
}

const resetImagePosition = () => {
  if (!originalImage) return

  const scaleX = CANVAS_SIZE / originalImage.width
  const scaleY = CANVAS_SIZE / originalImage.height
  imageScale = Math.min(scaleX, scaleY)

  imageX = (CANVAS_SIZE - originalImage.width * imageScale) / 2
  imageY = (CANVAS_SIZE - originalImage.height * imageScale) / 2
  rotation = 0
  zoomLevel.value = 100
}

const drawCanvas = () => {
  if (!canvasRef.value || !originalImage) return

  const canvas = canvasRef.value
  canvas.width = CANVAS_SIZE
  canvas.height = CANVAS_SIZE
  canvasCtx = canvas.getContext('2d')
  if (!canvasCtx) return

  canvasCtx.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE)

  const scale = imageScale * (zoomLevel.value / 100)

  canvasCtx.save()
  canvasCtx.translate(CANVAS_SIZE / 2, CANVAS_SIZE / 2)
  canvasCtx.rotate((rotation * Math.PI) / 180)
  canvasCtx.translate(-CANVAS_SIZE / 2, -CANVAS_SIZE / 2)

  canvasCtx.drawImage(
    originalImage,
    imageX + CANVAS_SIZE / 2 - (originalImage.width * scale) / 2,
    imageY + CANVAS_SIZE / 2 - (originalImage.height * scale) / 2,
    originalImage.width * scale,
    originalImage.height * scale
  )

  canvasCtx.restore()
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
      emit('cropper-show')
      setTimeout(drawCanvas, 50)
    }
    img.src = previewUrl.value
  }
}

const cancelCrop = () => {
  showCropper.value = false
  emit('cropper-hide')
  if (!previewUrl.value) {
    originalImage = null
  }
}

const confirmCrop = () => {
  if (!canvasRef.value || !canvasCtx || !originalImage) return

  const croppedCanvas = document.createElement('canvas')
  croppedCanvas.width = CANVAS_SIZE
  croppedCanvas.height = CANVAS_SIZE
  const ctx = croppedCanvas.getContext('2d')
  if (!ctx) return

  const scale = imageScale * (zoomLevel.value / 100)

  ctx.save()
  ctx.translate(CANVAS_SIZE / 2, CANVAS_SIZE / 2)
  ctx.rotate((rotation * Math.PI) / 180)
  ctx.translate(-CANVAS_SIZE / 2, -CANVAS_SIZE / 2)

  ctx.drawImage(
    originalImage,
    imageX + CANVAS_SIZE / 2 - (originalImage.width * scale) / 2,
    imageY + CANVAS_SIZE / 2 - (originalImage.height * scale) / 2,
    originalImage.width * scale,
    originalImage.height * scale
  )

  ctx.restore()

  const dataUrl = croppedCanvas.toDataURL('image/png')
  previewUrl.value = dataUrl
  emit('update:modelValue', dataUrl)
  showCropper.value = false
  emit('cropper-hide')
}

const removeAvatar = () => {
  previewUrl.value = ''
  emit('update:modelValue', '')
  originalImage = null
}
</script>

<style scoped>
.avatar-uploader {
  display: inline-block;
}

.avatar-preview {
  position: relative;
  width: 96px;
  height: 96px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 50%;
}

.avatar-preview:hover .avatar-overlay {
  opacity: 1;
}

.avatar-overlay button {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  cursor: pointer;
  transition: background 0.2s;
  border: none;
}

.avatar-overlay button:hover {
  background: rgba(255, 255, 255, 0.3);
}

.avatar-overlay .remove-btn {
  background: rgba(239, 68, 68, 0.6);
}

.avatar-overlay .remove-btn:hover {
  background: rgba(239, 68, 68, 0.8);
}

.avatar-input {
  display: inline-block;
}

.upload-menu {
  position: relative;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #64748b;
  background: transparent;
  border: none;
  padding: 0;
}

.upload-placeholder:hover {
  color: #818cf8;
}

.avatar-circle {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  border: 2px dashed #334155;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color 0.3s;
}

.upload-placeholder:hover .avatar-circle {
  border-color: #6366f1;
}

.menu-dropdown {
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  margin-top: 8px;
  background: #1e293b;
  border: 1px solid #334155;
  border-radius: 8px;
  overflow: hidden;
  z-index: 10;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  min-width: 140px;
}

.menu-item {
  width: 100%;
  padding: 10px 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #cbd5e1;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
  text-align: left;
  font-size: 13px;
}

.menu-item:hover {
  background: #334155;
  color: white;
}

.cropper-wrapper {
  background: #1e293b;
  border-radius: 12px;
  padding: 16px;
}

.cropper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  color: #fff;
  font-size: 14px;
}

.cropper-canvas-container {
  background: #0f172a;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px;
  position: relative;
}

.circle-mask {
  width: 300px;
  height: 300px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #6366f1;
  position: relative;
  z-index: 1;
}

.circle-mask canvas {
  cursor: move;
  max-width: 100%;
  position: relative;
  z-index: 1;
  pointer-events: auto;
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
  width: 100px;
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
  width: 40px;
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
