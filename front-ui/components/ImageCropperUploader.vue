<template>
  <div class="relative" @dragover.prevent @drop.prevent="onDrop">
    <!-- 默认触发区域 -->
    <div
      v-if="!previewUrl && !localPreview"
      class="upload-trigger"
      @click="openFilePicker"
      @paste.prevent="onPaste"
      tabindex="0"
      @keydown.enter="openFilePicker"
    >
      <div class="upload-placeholder">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15v4a2 2 0 01-2 2H5a2 2 0 01-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
        <span class="upload-text"><slot /></span>
      </div>
    </div>

    <!-- 已有图片预览 -->
    <div v-else class="preview-wrap" :style="{ aspectRatio: String(aspectRatio) }" @click="openFilePicker" @paste.prevent="onPaste">
      <img :src="localPreview || previewUrl" class="w-full h-full object-cover" />
      <div class="preview-overlay">
        <span class="preview-label">点击更换 / 裁剪</span>
      </div>
    </div>

    <input ref="$file" type="file" accept="image/png,image/jpeg,image/webp" class="hidden" @change="onFileChange" />

    <Teleport to="body">
      <div v-if="show" class="crop-mask" @click.self="show = false">
        <div class="crop-panel">
          <div class="crop-header">
            <h3>裁剪图片</h3>
            <div class="crop-tools">
              <button @click="zoomIn">+</button>
              <button @click="zoomOut">-</button>
              <button @click="rotate(-90)">⟲</button>
              <button @click="rotate(90)">⟳</button>
            </div>
          </div>
          <div class="crop-body" :class="{ 'crop-round': isRound }">
            <VueCropper ref="$cropper" :img="src" :autoCrop="true" :fixed="fixed" :fixedNumber="ratio" :canMove="true" :canMoveBox="true" :canScale="true" :centerBox="true" :full="true" :high="true" outputType="png" />
          </div>
          <div class="crop-footer">
            <button @click="cancel" class="btn-cancel">取消</button>
            <button @click="confirm" class="btn-confirm">确定裁剪</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { VueCropper } from 'vue-cropper'

const props = withDefaults(defineProps<{
  modelValue?: string
  aspectRatio?: number
  isRound?: boolean
}>(), { modelValue: '', aspectRatio: 1, isRound: false })

const emit = defineEmits<{ 'update:modelValue': [v: string]; 'crop-success': [f: File] }>()

const $file = ref<HTMLInputElement>()
const $cropper = ref<InstanceType<typeof VueCropper>>()
const show = ref(false)
const src = ref('')
const local = ref('')

const previewUrl = computed(() => props.modelValue)
const fixed = computed(() => props.aspectRatio > 0)
const ratio = computed<[number, number]>(() => [props.aspectRatio, 1])

function open() { $file.value?.click() }
function onFileChange(e: Event) {
  const f = (e.target as HTMLInputElement).files?.[0]
  if (f) readFile(f); (e.target as HTMLInputElement).value = ''
}
function onPaste(e: ClipboardEvent) {
  const f = e.clipboardData?.items?.[0]?.getAsFile()
  if (f && f.type.startsWith('image/')) readFile(f)
}
function onDrop(e: DragEvent) {
  const f = e.dataTransfer?.files?.[0]
  if (f?.type.startsWith('image/')) readFile(f)
}
function readFile(f: File) {
  const r = new FileReader()
  r.onload = () => { src.value = r.result as string; show.value = true }
  r.readAsDataURL(f)
}
function zoomIn() { $cropper.value?.zoom(0.2) }
function zoomOut() { $cropper.value?.zoom(-0.2) }
function rotate(deg: number) { $cropper.value?.rotateLeft ? $cropper.value.rotateLeft() : null; if (deg > 0) $cropper.value?.rotateRight() }
function cancel() { show.value = false }
function confirm() {
  $cropper.value?.getCropBlob((blob: Blob) => {
    const f = new File([blob], 'crop.png', { type: 'image/png' })
    const url = URL.createObjectURL(blob)
    local.value = url; show.value = false
    emit('update:modelValue', url)
    emit('crop-success', f)
  })
}
</script>

<style scoped>
.upload-trigger { @apply border-2 border-dashed border-slate-600 hover:border-emerald-500 rounded-xl cursor-pointer transition-colors flex items-center justify-center min-h-[140px]; }
.upload-trigger:focus-visible { @apply ring-2 ring-emerald-500 outline-none; }
.upload-placeholder { @apply flex flex-col items-center gap-2 text-slate-500; }
.upload-trigger:hover .upload-placeholder { @apply text-emerald-400; }
.preview-wrap { @apply relative rounded-xl overflow-hidden border-2 border-dashed cursor-pointer; }
.preview-overlay { @apply absolute inset-0 bg-black/50 opacity-0 hover:opacity-100 transition-opacity flex items-center justify-center; }
.preview-label { @apply text-white text-sm font-bold bg-white/20 px-4 py-2 rounded-lg backdrop-blur-sm; }
.crop-mask { @apply fixed inset-0 z-[200] flex items-center justify-center p-4; background: rgba(0,0,0,0.8); backdrop-filter: blur(8px); }
.crop-panel { @apply relative z-10 w-full max-w-2xl bg-slate-900 rounded-3xl border border-slate-700/50 shadow-2xl overflow-hidden; }
.crop-header { @apply p-4 border-b border-slate-700/50 flex items-center justify-between; }
.crop-header h3 { @apply text-lg font-bold text-white; }
.crop-tools { @apply flex items-center gap-2; }
.crop-tools button { @apply px-3 py-1 rounded-lg text-sm text-slate-400 hover:text-white hover:bg-slate-800 transition-colors; }
.crop-body { @apply bg-slate-950/50 p-4; }
.crop-round :deep(.cropper-face) { border-radius: 50% !important; }
.crop-footer { @apply p-4 border-t border-slate-700/50 flex justify-end gap-3; }
.btn-cancel { @apply px-6 py-2 rounded-xl font-bold bg-slate-700 text-slate-300 hover:bg-slate-600 transition-colors; }
.btn-confirm { @apply px-6 py-2 rounded-xl font-bold bg-gradient-to-r from-emerald-500 to-teal-500 text-white shadow-lg hover:shadow-emerald-500/30 transition-all; }
</style>
