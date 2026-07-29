// 客户端-only：app 挂载完成（hydration 之后）再执行 localStorage→cookie 兼容迁移
// 与用户信息拉取，避免在 store setup 阶段读 localStorage 引发的 SSR/hydration DOM 不一致。
export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.hook('app:mounted', () => {
    const user = useUserStore()
    user.hydrateClient()
  })
})
