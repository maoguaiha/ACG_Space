import request from './request'

export async function loginApi(username: string, password: string) {
  const res = await request.post<{ token: string }>('/auth/login', { username, password })
  return res.data
}

export async function fetchMe() {
  const res = await request.get<{ nickName?: string; userName?: string; avatar?: string }>('/auth/me')
  return res.data
}
