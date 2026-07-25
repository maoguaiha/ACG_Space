import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import axios from 'axios'
import router from './router'
import App from './App.vue'
import './style.css'

// 全局 axios 请求拦截器：自动附加 Token
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('acg_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 全局 axios 响应拦截器：401 跳转登录页
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('acg_token')
      if (router.currentRoute.value.path !== '/login') {
        router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
      }
    }
    return Promise.reject(error)
  }
)

// 全局图片加载失败兜底：外链 CDN（如 Bangumi 封面图）在本机无公网出口时无法访问，
// 任何 <img> 加载失败都换成内置占位图，避免出现破图 / 控制台刷屏。
const setupImageFallback = () => {
  const FALLBACK = '/img-fallback.svg'
  document.addEventListener(
    'error',
    (event: Event) => {
      const target = event.target as HTMLElement | null
      if (target && target.tagName === 'IMG') {
        const img = target as HTMLImageElement
        if (img.dataset.fallbackApplied) return
        img.dataset.fallbackApplied = '1'
        img.src = FALLBACK
      }
    },
    true
  )
}
setupImageFallback()

const app = createApp(App)
const pinia = createPinia()

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, { size: 'default' })

app.mount('#app')
