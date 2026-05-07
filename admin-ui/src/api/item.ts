import axios from 'axios'

const baseUrl = '/api'

export interface Item {
  id?: number
  itemKey: string
  name: string
  type: string
  rarity: string
  image?: string
  description?: string
  totalStock?: number
  remainingStock?: number
  price?: number
  marketable?: boolean
  synthesizable?: boolean
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  remark?: string
  delFlag?: number
}

export interface ItemPageResult {
  records: Item[]
  total: number
  size: number
  current: number
}

export const itemApi = {
  page(params: {
    pageNum: number
    pageSize: number
    name?: string
    rarity?: string
    type?: string
  }) {
    return axios.get<{ data: ItemPageResult }>(`${baseUrl}/item/page`, { params })
  },

  getById(id: number) {
    return axios.get<{ data: Item }>(`${baseUrl}/item/${id}`)
  },

  getByItemKey(itemKey: string) {
    return axios.get<{ data: Item }>(`${baseUrl}/item/key/${itemKey}`)
  },

  create(data: Item) {
    return axios.post(`${baseUrl}/item`, data)
  },

  update(data: Item) {
    return axios.put(`${baseUrl}/item`, data)
  },

  delete(id: number) {
    return axios.delete(`${baseUrl}/item/${id}`)
  }
}
