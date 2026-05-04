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

  // 初始化从 cookie/localStorage 加载 token（cookie 兼容 SSR，localStorage 兼容旧数据）
  if (import.meta.client) {
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
      // 延迟获取用户信息，确保在客户端环境下
      nextTick(() => {
        loadUserInfo()
      })
    }
  }

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
    setToken,
    setUser,
    logout
  }
})
