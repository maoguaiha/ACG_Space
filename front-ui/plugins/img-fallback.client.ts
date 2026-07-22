// 全局图片加载失败兜底（仅客户端）
// 监听 document 的 error 事件（捕获阶段），任何 <img> 加载失败都换成内置占位图。
// 这样即使外链 CDN（如 Bangumi 封面图）在本机无公网出口时无法访问，也不会出现破图。
export default defineNuxtPlugin(() => {
  if (import.meta.client) {
    const FALLBACK = '/img-fallback.svg'
    const onImgError = (event: Event) => {
      const target = event.target as HTMLElement | null
      if (target && target.tagName === 'IMG') {
        const img = target as HTMLImageElement
        // 防止占位图自身也失败导致无限循环
        if (img.dataset.fallbackApplied) return
        img.dataset.fallbackApplied = '1'
        img.src = FALLBACK
      }
    }
    document.addEventListener('error', onImgError, true)
  }
})
