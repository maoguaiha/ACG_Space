import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动附加 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('acg_token')
    if (token) {
      config.headers.set('Authorization', `Bearer ${token}`)
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：401 通知
// （重定向逻辑在 auth store 里处理，避免循环依赖）
request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('acg_token')
      window.dispatchEvent(new CustomEvent('auth:401'))
    }
    return Promise.reject(error)
  }
)

export default request
