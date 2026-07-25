import request from './request'

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
    return request.get<{ data: ItemPageResult }>('/item/page', { params })
  },

  getById(id: number) {
    return request.get<{ data: Item }>('/item/${id}')
  },

  getByItemKey(itemKey: string) {
    return request.get<{ data: Item }>('/item/key/${itemKey}')
  },

  create(data: Item) {
    return request.post('/item', data)
  },

  update(data: Item) {
    return request.put('/item', data)
  },

  delete(id: number) {
    return request.delete('/item/${id}')
  }
}
