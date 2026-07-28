/**
 * v-reveal 自定义指令
 * 页面滚动时元素进入视口则触发显现动画
 * 用法: <div v-reveal>内容</div>
 * 可传延迟值: <div v-reveal="0.2">延迟0.2秒</div>
 */
export default defineNuxtPlugin((nuxtApp) => {
  if (import.meta.server) return

  nuxtApp.vueApp.directive('reveal', {
    mounted(el: HTMLElement, binding) {
      // 添加基础类
      el.classList.add('reveal')

      // 延迟（秒）
      const delay = binding.value || 0
      if (delay > 0) {
        el.style.transitionDelay = `${delay}s`
      }

      // 使用 IntersectionObserver
      const observer = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              el.classList.add('revealed')
              observer.unobserve(el)
            }
          })
        },
        { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
      )

      observer.observe(el)

      // 保存 observer 以便 unmounted 时清理
      ;(el as any).__revealObserver = observer
    },
    unmounted(el: HTMLElement) {
      if ((el as any).__revealObserver) {
        ;(el as any).__revealObserver.unobserve(el)
      }
    },
  })
})
