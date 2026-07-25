import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios'

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 响应拦截器：401 通知
instance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('acg_token')
      window.dispatchEvent(new CustomEvent('auth:401'))
    }
    return Promise.reject(error)
  }
)

// 不依赖拦截器注入 token——直接在每次调用时注入
function withAuth(config: AxiosRequestConfig = {}): AxiosRequestConfig {
  const token = localStorage.getItem('acg_token')
  return token
    ? { ...config, headers: { ...config.headers, Authorization: `Bearer ${token}` } }
    : config
}

const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return instance.get(url, withAuth(config))
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return instance.post(url, data, withAuth(config))
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return instance.put(url, data, withAuth(config))
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>> {
    return instance.delete(url, withAuth(config))
  }
}

export default request
