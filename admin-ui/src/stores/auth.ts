import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginApi, fetchMe } from '../api/auth'
import { ElMessage } from 'element-plus'
import router from '../router'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('acg_token'))
  const userInfo = ref<{ nickName?: string; userName?: string; avatar?: string } | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const displayName = computed(() => userInfo.value?.nickName || userInfo.value?.userName || 'Admin')
  const avatar = computed(() => userInfo.value?.avatar || '')

  // 监听请求层的 401 事件（避免 request.ts 里 import router 造成循环依赖）
  if (typeof window !== 'undefined') {
    window.addEventListener('auth:401', () => {
      token.value = null
      userInfo.value = null
      if (router.currentRoute.value.path !== '/login') {
        router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
      }
    })
  }

  async function login(username: string, password: string) {
    const res = await loginApi(username, password)
    token.value = res.token
    localStorage.setItem('acg_token', res.token)
    // 获取用户信息
    try {
      userInfo.value = await fetchMe()
    } catch {
      // 获取用户信息失败不阻塞登录
    }
  }

  async function loadUser() {
    if (!token.value) return
    try {
      userInfo.value = await fetchMe()
    } catch {
      logout()
    }
  }

  function logout() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('acg_token')
  }

  function logoutAndRedirect() {
    logout()
    ElMessage.success('已注销')
    router.replace('/login')
  }

  return { token, userInfo, isLoggedIn, displayName, avatar, login, loadUser, logout, logoutAndRedirect }
})
