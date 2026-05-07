/**
 * 认证守卫 - 检查用户登录状态，未登录则跳转到登录页
 */
export const useAuthGuard = () => {
  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()

  const requireAuth = (callback?: () => void) => {
    if (!userStore.isLoggedIn) {
      const currentPath = route.fullPath
      router.push({
        path: '/login',
        query: { redirect: currentPath }
      })
      return false
    }
    if (callback) {
      callback()
    }
    return true
  }

  return {
    requireAuth,
    isLoggedIn: computed(() => userStore.isLoggedIn)
  }
}