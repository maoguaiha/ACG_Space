<template>
  <div class="square-image-uploader">
    <div class="image-preview" v-if="previewUrl && !showCropper">
      <img :src="previewUrl" alt="图片预览" />
      <div class="image-actions">
        <el-button size="small" @click="editImage">重新编辑</el-button>
        <el-button size="small" type="danger" @click="removeImage">删除</el-button>
      </div>
    </div>

    <div class="image-input" v-else-if="!showCropper">
      <input
        ref="fileInputRef"
        type="file"
        accept="image/*"
        @change="handleFileSelect"
        style="display: none"
      />
      <div class="upload-placeholder" @click="triggerFileSelect" @paste="handlePaste">
        <el-icon class="upload-icon"><Plus /></el-icon>
        <div class="upload-text">
          <span>点击选择图片</span>
          <span class="upload-hint">或 Ctrl+V 粘贴图片</span>
        </div>
      </div>
    </div>

    <div v-if="showCropper" class="cropper-wrapper">
      <div class="cropper-header">
        <span>裁剪图片 (正方形 1:1)</span>
        <el-button size="small" @click="cancelCrop">取消</el-button>
      </div>
      <div class="cropper-canvas-container">
        <canvas ref="canvasRef" @mousedown="startDrag" @mousemove="doDrag" @mouseup="endDrag" @wheel="doZoom"></canvas>
      </div>
      <div class="cropper-toolbar">
        <el-button @click="rotateLeft" icon="RefreshLeft">左旋转</el-button>
        <el-button @click="rotateRight" icon="RefreshRight">右旋转</el-button>
        <el-slider v-model="zoomLevel" :min="50" :max="200" style="width: 150px" />
        <span class="zoom-label">{{ zoomLevel }}%</span>
        <el-button type="primary" @click="confirmCrop">确认</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

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
const ASPECT_RATIO = 1

watch(() => props.modelValue, (val) => {
  previewUrl.value = val || ''
})

const triggerFileSelect = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) {
    loadImageForCropper(file)
  }
  input.value = ''
}

const handlePaste = (event: ClipboardEvent) => {
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

const loadImageForCropper = (file: File) => {
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过10MB')
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

  imageScale = CANVAS_SIZE / Math.max(originalImage.width, originalImage.height)
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

  canvasCtx.fillStyle = '#1a1a1a'
  canvasCtx.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE)

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

  canvasCtx.strokeStyle = '#409eff'
  canvasCtx.lineWidth = 2
  canvasCtx.setLineDash([5, 5])
  canvasCtx.strokeRect(0, 0, CANVAS_SIZE, CANVAS_SIZE)
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
  if (!canvasRef.value || !canvasCtx) return

  const croppedCanvas = document.createElement('canvas')
  croppedCanvas.width = CANVAS_SIZE
  croppedCanvas.height = CANVAS_SIZE
  const ctx = croppedCanvas.getContext('2d')
  if (!ctx) return

  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE)

  const scale = imageScale * (zoomLevel.value / 100)

  ctx.save()
  ctx.translate(CANVAS_SIZE / 2, CANVAS_SIZE / 2)
  ctx.rotate((rotation * Math.PI) / 180)
  ctx.translate(-CANVAS_SIZE / 2, -CANVAS_SIZE / 2)

  ctx.drawImage(
    originalImage!,
    imageX + CANVAS_SIZE / 2 - (originalImage!.width * scale) / 2,
    imageY + CANVAS_SIZE / 2 - (originalImage!.height * scale) / 2,
    originalImage!.width * scale,
    originalImage!.height * scale
  )

  ctx.restore()

  const dataUrl = croppedCanvas.toDataURL('image/png', 0.9)
  previewUrl.value = dataUrl
  emit('update:modelValue', dataUrl)
  showCropper.value = false
}

const removeImage = () => {
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
.square-image-uploader {
  width: 100%;
}

.image-preview {
  position: relative;
  width: 200px;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
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

.image-preview:hover .image-actions {
  opacity: 1;
}

.image-input {
  width: 200px;
}

.upload-placeholder {
  width: 200px;
  height: 200px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-placeholder:hover {
  border-color: #409eff;
}

.upload-icon {
  font-size: 40px;
  color: #c0c4cc;
}

.upload-text {
  margin-top: 8px;
  text-align: center;
  color: #606266;
}

.upload-hint {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.cropper-wrapper {
  background: #2a2a2a;
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
  background: #1a1a1a;
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

.zoom-label {
  font-size: 12px;
  color: #909399;
  width: 45px;
}
</style>