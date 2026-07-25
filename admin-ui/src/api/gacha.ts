import request from './request'

export interface GachaPool {
  id?: number
  name: string
  description?: string
  banner?: string
  rarity: string
  totalStock?: number
  remainingStock?: number
  singleCost?: number
  tenCost?: number
  guaranteeCount?: number
  guaranteeType?: string
  startTime?: string
  endTime?: string
  status?: number
  weightConfig?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  remark?: string
  delFlag?: number
}

export interface GachaPoolPageResult {
  records: GachaPool[]
  total: number
  size: number
  current: number
}

export interface GachaPoolItem {
  id?: number
  poolId: number
  itemId: number
  rarity: string
  weight: number
  isGuarantee?: boolean
  stockLimit?: number
  createTime?: string
}

export interface GachaPrize {
  id?: number
  poolId?: number
  itemName: string
  itemImage: string
  rarity: string
  quantity: number
  weight: number
  createTime?: string
}

export const gachaApi = {
  page(params: {
    pageNum: number
    pageSize: number
    name?: string
    status?: number
  }) {
    return request.get<{ data: GachaPoolPageResult }>('/gacha/page', { params })
  },

  getActivePools() {
    return request.get<{ data: GachaPool[] }>('/gacha/active')
  },

  getById(id: number) {
    return request.get<{ data: GachaPool }>('/gacha/${id}')
  },

  create(data: GachaPool) {
    return request.post('/gacha', data)
  },

  update(data: GachaPool) {
    return request.put('/gacha', data)
  },

  endPool(id: number) {
    return request.post('/gacha/${id}/end')
  },

  getPool(id: number) {
    return request.get<{ data: GachaPool }>('/gacha/${id}')
  },

  getPrizes(poolId: number) {
    return request.get<{ data: GachaPrize[] }>('/gacha/${poolId}/prizes')
  },

  createPrize(poolId: number, data: Omit<GachaPrize, 'id' | 'poolId'>) {
    return request.post('/gacha/${poolId}/prizes', data)
  },

  updatePrize(data: GachaPrize) {
    return request.put('/gacha/prizes', data)
  },

  deletePrize(id: number) {
    return request.delete('/gacha/prizes/${id}')
  }
}
