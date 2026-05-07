import axios from 'axios'

const baseUrl = '/api'

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
    return axios.get<{ data: GachaPoolPageResult }>(`${baseUrl}/gacha/page`, { params })
  },

  getActivePools() {
    return axios.get<{ data: GachaPool[] }>(`${baseUrl}/gacha/active`)
  },

  getById(id: number) {
    return axios.get<{ data: GachaPool }>(`${baseUrl}/gacha/${id}`)
  },

  create(data: GachaPool) {
    return axios.post(`${baseUrl}/gacha`, data)
  },

  update(data: GachaPool) {
    return axios.put(`${baseUrl}/gacha`, data)
  },

  endPool(id: number) {
    return axios.post(`${baseUrl}/gacha/${id}/end`)
  },

  getPool(id: number) {
    return axios.get<{ data: GachaPool }>(`${baseUrl}/gacha/${id}`)
  },

  getPrizes(poolId: number) {
    return axios.get<{ data: GachaPrize[] }>(`${baseUrl}/gacha/${poolId}/prizes`)
  },

  createPrize(poolId: number, data: Omit<GachaPrize, 'id' | 'poolId'>) {
    return axios.post(`${baseUrl}/gacha/${poolId}/prizes`, data)
  },

  updatePrize(data: GachaPrize) {
    return axios.put(`${baseUrl}/gacha/prizes`, data)
  },

  deletePrize(id: number) {
    return axios.delete(`${baseUrl}/gacha/prizes/${id}`)
  }
}
