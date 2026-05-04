<script setup lang="ts">
import { registerApi } from '~/composables/useApi'

const router = useRouter()

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const loading = ref(false)
const errorMsg = ref('')

const handleRegister = async () => {
  if (!form.username || !form.nickname || !form.password) {
    errorMsg.value = '请填写所有必填项'
    return
  }
  if (form.password !== form.confirmPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }

  loading.value = true
  errorMsg.value = ''
  
  try {
    await registerApi({
      username: form.username,
      nickname: form.nickname,
      password: form.password
    })
    
    // 注册成功跳转登录
    alert('注册成功，请登录')
    router.push('/login')
  } catch (err: any) {
    errorMsg.value = err.message || '注册失败，用户名可能已存在'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center px-4 py-12 relative overflow-hidden">
    <!-- Background Glow -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div class="absolute -top-[10%] -right-[10%] w-[40%] h-[40%] bg-purple-500/10 rounded-full blur-[120px]"></div>
      <div class="absolute -bottom-[10%] -left-[10%] w-[40%] h-[40%] bg-indigo-500/10 rounded-full blur-[120px]"></div>
    </div>

    <div class="max-w-md w-full space-y-8 relative z-10">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-white tracking-tight">
          加入 ACG Space
        </h2>
        <p class="mt-2 text-center text-sm text-slate-400">
          开启你的番剧收藏之旅
        </p>
      </div>
      
      <form class="mt-8 space-y-6" @submit.prevent="handleRegister">
        <div class="rounded-2xl bg-white/5 backdrop-blur-xl border border-white/10 p-8 shadow-2xl space-y-4">
          <div>
            <label for="username" class="block text-sm font-medium text-slate-300 mb-1">用户名</label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              required
              class="appearance-none block w-full px-4 py-3 bg-slate-900/50 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
              placeholder="用于登录的账号"
            />
          </div>
          <div>
            <label for="nickname" class="block text-sm font-medium text-slate-300 mb-1">昵称</label>
            <input
              id="nickname"
              v-model="form.nickname"
              type="text"
              required
              class="appearance-none block w-full px-4 py-3 bg-slate-900/50 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
              placeholder="你的展示名称"
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
          <div>
            <label for="confirmPassword" class="block text-sm font-medium text-slate-300 mb-1">确认密码</label>
            <input
              id="confirmPassword"
              v-model="form.confirmPassword"
              type="password"
              required
              class="appearance-none block w-full px-4 py-3 bg-slate-900/50 border border-slate-700 rounded-xl text-white placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition-all"
              placeholder="请再次输入密码"
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
                正在注册...
              </span>
              <span v-else>立即注册</span>
            </button>
          </div>
        </div>

        <div class="text-center">
          <p class="text-sm text-slate-400">
            已有账号？
            <NuxtLink to="/login" class="font-medium text-indigo-400 hover:text-indigo-300 transition-colors">
              返回登录
            </NuxtLink>
          </p>
        </div>
      </form>
    </div>
  </div>
</template>
