import axios from 'axios'

const BASE = '/api'

export async function loginApi(username: string, password: string) {
  const res = await axios.post<{ token: string }>(`${BASE}/auth/login`, { username, password })
  return res.data
}

export async function fetchMe() {
  const res = await axios.get<{ nickName?: string; userName?: string; avatar?: string }>(`${BASE}/system/user/myProfile`)
  return res.data
}
