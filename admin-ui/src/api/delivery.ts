import axios from 'axios'

const baseUrl = '/api'

export interface DeliveryOrder {
  id?: number
  orderId: string
  userId: number
  userName?: string
  userAvatar?: string
  assetId: number
  itemId: number
  itemName: string
  itemImage?: string
  itemRarity?: string
  receiver: string
  phone: string
  address: string
  expressCompany?: string
  expressNo?: string
  remark?: string
  status?: number
  shipTime?: string
  completeTime?: string
  createTime?: string
  updateTime?: string
}

export interface DeliveryPageResult {
  records: DeliveryOrder[]
  total: number
  size: number
  current: number
}

export interface DeliveryStats {
  pending: number
  shipped: number
  completed: number
  monthlyRedeem: number
}

export interface ShipRequest {
  orderId: string
  expressCompany: string
  expressNo: string
  remark?: string
}

export const deliveryApi = {
  page(params: {
    pageNum: number
    pageSize: number
    orderId?: string
    status?: number
  }) {
    return axios.get<{ data: DeliveryPageResult }>(`${baseUrl}/delivery/page`, { params })
  },

  getStats() {
    return axios.get<{ data: DeliveryStats }>(`${baseUrl}/delivery/stats`)
  },

  getByOrderId(orderId: string) {
    return axios.get<{ data: DeliveryOrder }>(`${baseUrl}/delivery/${orderId}`)
  },

  ship(data: ShipRequest) {
    return axios.post(`${baseUrl}/delivery/ship`, data)
  },

  complete(orderId: string) {
    return axios.post(`${baseUrl}/delivery/${orderId}/complete`)
  },

  cancel(orderId: string) {
    return axios.post(`${baseUrl}/delivery/${orderId}/cancel`)
  }
}
