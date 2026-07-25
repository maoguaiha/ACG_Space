import request from './request'

export async function loginApi(username: string, password: string) {
  const res = await request.post<{ code: number; msg: string; data: { token: string } }>('/auth/login', { username, password })
  return res.data.data  // 后端用 Result<T> 包装，真实数据在 .data 里
}

export async function fetchMe() {
  const res = await request.get<{ code: number; msg: string; data: { nickName?: string; userName?: string; avatar?: string } }>('/auth/me')
  return res.data.data
}
