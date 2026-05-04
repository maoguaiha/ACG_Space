import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const message = ref<{ text: string, type: 'success' | 'error' | 'info' } | null>(null)
  let timer: any = null

  function showMessage(text: string, type: 'success' | 'error' | 'info' = 'success', duration = 3000) {
    message.value = { text, type }
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      message.value = null
    }, duration)
  }

  return {
    message,
    showMessage
  }
})
