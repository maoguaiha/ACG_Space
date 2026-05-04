<script setup lang="ts">
import { useUserStore } from '~/stores/user'
import { loginApi, fetchUserInfo } from '~/composables/useApi'

const userStore = useUserStore()
const router = useRouter()

const form = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const errorMsg = ref('')

const loginRoute = useRoute()

const handleLogin = async () => {
  if (!form.username || !form.password) {
    errorMsg.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  errorMsg.value = ''
  
  try {
    const { token } = await loginApi(form)
    userStore.setToken(token)
    
    // 获取用户信息
    const userInfo = await fetchUserInfo()
    userStore.setUser(userInfo)
    
    // 跳转回上一页或首页
    const redirect = (loginRoute.query.redirect as string) || '/'
    const otherQuery = { ...loginRoute.query }
    delete otherQuery.redirect
    
    router.push({ path: redirect, query: otherQuery })
  } catch (err: any) {
    errorMsg.value = err.message || '登录失败，请检查账号密码'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center px-4 py-12 relative overflow-hidden">
    <!-- Background Glow -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-[10%] -left-[10%] w-[40%] h-[40%] bg-indigo-500/10 rounded-full blur-[120px]"></div>
      <div class="absolute -bottom-[10%] -right-[10%] w-[40%] h-[40%] bg-purple-500/10 rounded-full blur-[120px]"></div>
    </div>

    <div class="max-w-md w-full space-y-8 relative z-10">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-white tracking-tight">
          欢迎回来
        </h2>
        <p class="mt-2 text-center text-sm text-slate-400">
          登录你的 ACG Space 账号
        </p>
      </div>
      
      <form class="mt-8 space-y-6" @submit.prevent="handleLogin">
        <div class="rounded-2xl bg-white/5 backdrop-blur-xl border border-white/10 p-8 shadow-2xl space-y-4">
          <div>
            <label for="username" class="block text-sm font-medium text-slate-300 mb-1">用户名</label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              required
              class="appearance-none block w-full px-4 py-3 bg-slate-900/50 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
              placeholder="请输入用户名"
            />
          </div>
          <div>
            <label for="password" class="block text-sm font-medium text-slate-300 mb-1">密码</label>
            <input
              id="password"
              v-model="form.password"
              type="password"
              required
              class="appearance-none block w-full px-4 py-3 bg-slate-900/50 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
              placeholder="请输入密码"
            />
          </div>

          <div v-if="errorMsg" class="text-rose-500 text-sm animate-pulse">
            {{ errorMsg }}
          </div>

          <div>
            <button
              type="submit"
              :disabled="loading"
              class="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-semibold rounded-xl text-white bg-indigo-600 hover:bg-indigo-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="loading" class="flex items-center">
                <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                正在登录...
              </span>
              <span v-else>立即登录</span>
            </button>
          </div>
        </div>

        <div class="text-center">
          <p class="text-sm text-slate-400">
            还没有账号？
            <NuxtLink to="/register" class="font-medium text-indigo-400 hover:text-indigo-300 transition-colors">
              立即注册
            </NuxtLink>
          </p>
        </div>
      </form>
    </div>
  </div>
</template>
