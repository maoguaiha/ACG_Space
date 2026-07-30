import { defineStore } from 'pinia'

interface UserInfo {
  id: string
  username: string
  nickname: string
  avatar: string
}

export const useUserStore = defineStore('user', () => {
  const tokenCookie = useCookie<string | null>('acg_token', {
    default: () => null,
    sameSite: 'lax'
  })
  const token = ref<string | null>(tokenCookie.value)
  const userInfo = ref<UserInfo | null>(null)

  // SSR 安全：初始 token 只来自 cookie（SSR 与客户端 hydration 阶段一致），
  // localStorage 的兼容迁移不在此处执行，否则在 store 顶层调用 onMounted 会
  // "no active component instance" 警告且不会真正注册；改为暴露 hydrateClient()
  // action，由 plugins/user-init.client.ts 在 app:mounted（hydration 之后）调用。
  const isLoggedIn = computed(() => !!token.value)

  async function loadUserInfo() {
    if (!token.value) return
    try {
      const { fetchMe } = await import('~/composables/useApi')
      const user = await fetchMe()
      userInfo.value = user
    } catch (e) {
      console.error('Failed to load user info', e)
      // 如果 Token 过效，清除状态
      logout()
    }
  }

  function setToken(newToken: string) {
    token.value = newToken
    tokenCookie.value = newToken
    if (import.meta.client) {
      localStorage.setItem('acg_token', newToken)
    }
  }

  // 客户端 hydration 完成后调用：把 localStorage 中的旧 token 同步到 cookie，
  // 并拉取用户信息。放在 app:mounted 之后执行，避免 SSR/hydration DOM 不一致。
  function hydrateClient() {
    if (!token.value) {
      const savedToken = localStorage.getItem('acg_token')
      if (savedToken) {
        token.value = savedToken
        tokenCookie.value = savedToken
      }
    } else {
      // 保证本地缓存与 cookie 一致
      localStorage.setItem('acg_token', token.value)
    }

    if (token.value) {
      loadUserInfo()
    }
  }

  function setUser(user: UserInfo) {
    userInfo.value = user
  }

  function logout() {
    token.value = null
    tokenCookie.value = null
    userInfo.value = null
    if (import.meta.client) {
      localStorage.removeItem('acg_token')
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    hydrateClient,
    setToken,
    setUser,
    logout
  }
})
