<template>
  <div class="image-picker-dialog">
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
      <div class="upload-placeholder" @click="showPickerDialog = true" @paste="handlePaste">
        <el-icon class="upload-icon"><Plus /></el-icon>
        <div class="upload-text">
          <span>点击添加图片</span>
          <span class="upload-hint">或 Ctrl+V 粘贴图片</span>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="showPickerDialog"
      title="选择图片"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="picker-options">
        <div class="picker-option" @click="selectLocalFile">
          <el-icon class="option-icon"><Upload /></el-icon>
          <span class="option-text">选择本地文件</span>
          <span class="option-hint">支持 JPG、PNG、GIF 等格式</span>
        </div>
        <div class="picker-option" @click="selectClipboard">
          <el-icon class="option-icon"><DocumentCopy /></el-icon>
          <span class="option-text">粘贴图片</span>
          <span class="option-hint">复制图片后，按 Ctrl+V 粘贴</span>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="showPasteDialog"
      title="粘贴图片"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="paste-area" @paste="handlePasteInDialog">
        <div v-if="!pastedImage" class="paste-placeholder">
          <el-icon class="paste-icon"><DocumentCopy /></el-icon>
          <span>在此处按 Ctrl+V 粘贴图片</span>
        </div>
        <img v-else :src="pastedImage" alt="粘贴的图片" class="pasted-preview" />
      </div>
      <template #footer>
        <el-button @click="cancelPaste">取消</el-button>
        <el-button type="primary" @click="confirmPaste" :disabled="!pastedImage">确认</el-button>
      </template>
    </el-dialog>

    <div v-if="showCropper" class="cropper-wrapper">
      <div class="cropper-header">
        <span>裁剪图片</span>
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
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { Plus, Upload, DocumentCopy } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

interface Props {
  modelValue?: string
  aspectRatio?: number
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  aspectRatio: 16 / 9
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const fileInputRef = ref<HTMLInputElement>()
const canvasRef = ref<HTMLCanvasElement>()
const previewUrl = ref(props.modelValue || '')
const showCropper = ref(false)
const showPickerDialog = ref(false)
const showPasteDialog = ref(false)
const pastedImage = ref<string | null>(null)
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
let pendingImageData: string | null = null

const CANVAS_SIZE = 400

watch(() => props.modelValue, (newVal) => {
  previewUrl.value = newVal || ''
})

function selectLocalFile() {
  showPickerDialog.value = false
  setTimeout(() => {
    fileInputRef.value?.click()
  }, 100)
}

function selectClipboard() {
  showPickerDialog.value = false
  showPasteDialog.value = true
  pastedImage.value = null
}

function handlePaste(event: ClipboardEvent) {
  const items = event.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      event.preventDefault()
      const file = item.getAsFile()
      if (file) {
        processFile(file)
      }
      break
    }
  }
}

function handlePasteInDialog(event: ClipboardEvent) {
  event.preventDefault()
  const items = event.clipboardData?.items
  if (!items) return
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      const file = item.getAsFile()
      if (file) {
        const reader = new FileReader()
        reader.onload = (e) => {
          pastedImage.value = e.target?.result as string
        }
        reader.readAsDataURL(file)
      }
      break
    }
  }
}

function cancelPaste() {
  showPasteDialog.value = false
  pastedImage.value = null
}

function confirmPaste() {
  if (!pastedImage.value) {
    ElMessage.warning('请先粘贴图片')
    return
  }
  showPasteDialog.value = false
  pendingImageData = pastedImage.value
  startCropper(pastedImage.value)
  pastedImage.value = null
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (file) {
    processFile(file)
  }
  target.value = ''
}

function processFile(file: File) {
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    const result = e.target?.result as string
    pendingImageData = result
    startCropper(result)
  }
  reader.readAsDataURL(file)
}

function startCropper(imageData: string) {
  const img = new Image()
  img.onload = () => {
    originalImage = img
    imageX = 0
    imageY = 0
    imageScale = 1
    rotation = 0
    zoomLevel.value = 100
    showCropper.value = true
    setTimeout(drawCropper, 100)
  }
  img.src = imageData
}

function drawCropper() {
  if (!canvasRef.value || !originalImage) return
  const canvas = canvasRef.value
  canvas.width = CANVAS_SIZE
  canvas.height = CANVAS_SIZE
  canvasCtx = canvas.getContext('2d')
  if (!canvasCtx) return

  canvasCtx.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE)
  canvasCtx.save()
  canvasCtx.translate(CANVAS_SIZE / 2, CANVAS_SIZE / 2)
  canvasCtx.rotate((rotation * Math.PI) / 180)
  const scaledWidth = originalImage.width * imageScale * (zoomLevel.value / 100)
  const scaledHeight = originalImage.height * imageScale * (zoomLevel.value / 100)
  canvasCtx.drawImage(originalImage, -scaledWidth / 2 + imageX, -scaledHeight / 2 + imageY, scaledWidth, scaledHeight)
  canvasCtx.restore()

  canvasCtx.strokeStyle = '#409EFF'
  canvasCtx.lineWidth = 2
  const cropSize = CANVAS_SIZE * 0.8
  const cropX = (CANVAS_SIZE - cropSize) / 2
  const cropY = (CANVAS_SIZE - cropSize) / 2
  canvasCtx.strokeRect(cropX, cropY, cropSize, cropSize)
}

function startDrag(event: MouseEvent) {
  isDragging = true
  dragStartX = event.clientX
  dragStartY = event.clientY
}

function doDrag(event: MouseEvent) {
  if (!isDragging || !originalImage) return
  const deltaX = event.clientX - dragStartX
  const deltaY = event.clientY - dragStartY
  imageX += deltaX
  imageY += deltaY
  dragStartX = event.clientX
  dragStartY = event.clientY
  drawCropper()
}

function endDrag() {
  isDragging = false
}

function doZoom(event: WheelEvent) {
  event.preventDefault()
  const delta = event.deltaY > 0 ? -5 : 5
  zoomLevel.value = Math.max(50, Math.min(200, zoomLevel.value + delta))
  drawCropper()
}

function rotateLeft() {
  rotation -= 90
  drawCropper()
}

function rotateRight() {
  rotation += 90
  drawCropper()
}

function confirmCrop() {
  if (!canvasRef.value || !originalImage) return
  const cropSize = CANVAS_SIZE * 0.8
  const cropCanvas = document.createElement('canvas')
  cropCanvas.width = cropSize
  cropCanvas.height = cropSize
  const ctx = cropCanvas.getContext('2d')
  if (!ctx) return

  ctx.save()
  ctx.translate(cropSize / 2, cropSize / 2)
  ctx.rotate((rotation * Math.PI) / 180)
  const scaledWidth = originalImage.width * imageScale * (zoomLevel.value / 100)
  const scaledHeight = originalImage.height * imageScale * (zoomLevel.value / 100)
  ctx.drawImage(originalImage, -scaledWidth / 2 + imageX, -scaledHeight / 2 + imageY, scaledWidth, scaledHeight)
  ctx.restore()

  const croppedData = cropCanvas.toDataURL('image/jpeg', 0.9)
  previewUrl.value = croppedData
  emit('update:modelValue', croppedData)
  showCropper.value = false
}

function cancelCrop() {
  showCropper.value = false
  pendingImageData = null
}

function editImage() {
  if (previewUrl.value) {
    pendingImageData = previewUrl.value
    startCropper(previewUrl.value)
  }
}

function removeImage() {
  previewUrl.value = ''
  emit('update:modelValue', '')
}

function handleGlobalPaste(event: ClipboardEvent) {
  if (showPasteDialog.value) return
  handlePaste(event)
}

onMounted(() => {
  document.addEventListener('paste', handleGlobalPaste)
})

onUnmounted(() => {
  document.removeEventListener('paste', handleGlobalPaste)
})
</script>

<style scoped>
.image-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.image-preview img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
  object-fit: contain;
}

.image-actions {
  display: flex;
  gap: 8px;
}

.image-input {
  width: 100%;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 20px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-placeholder:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.upload-icon {
  font-size: 40px;
  color: #909399;
}

.upload-text {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #606266;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

.picker-options {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px 0;
}

.picker-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.picker-option:hover {
  border-color: #409eff;
  background: #f0f7ff;
}

.option-icon {
  font-size: 32px;
  color: #409eff;
}

.option-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.option-hint {
  font-size: 12px;
  color: #909399;
}

.paste-area {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  margin: 10px 0;
}

.paste-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #909399;
}

.paste-icon {
  font-size: 48px;
}

.pasted-preview {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  object-fit: contain;
}

.cropper-wrapper {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.cropper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.cropper-canvas-container {
  display: flex;
  justify-content: center;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.cropper-canvas-container canvas {
  max-width: 100%;
  cursor: move;
}

.cropper-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
}

.zoom-label {
  font-size: 14px;
  color: #606266;
  min-width: 45px;
}
</style>