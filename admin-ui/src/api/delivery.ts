import axios from 'axios'

const baseUrl = '/api/admin/redeem'

export interface DeliveryOrder {
  id?: number
  orderNo?: string
  userId?: number
  userName?: string
  userAvatar?: string
  assetId?: number
  itemId?: number
  itemName?: string
  itemImage?: string
  itemRarity?: string
  productId?: number
  productName?: string
  productImage?: string
  receiver?: string
  phone?: string
  province?: string
  city?: string
  district?: string
  address?: string
  logisticsCompany?: string
  logisticsNo?: string
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
  orderId: number
  logisticsCompany: string
  logisticsNo: string
}

export const deliveryApi = {
  page(params: {
    pageNum: number
    pageSize: number
    orderNo?: string
    status?: number
  }) {
    return axios.get<{ data: DeliveryPageResult }>(`${baseUrl}/orders`, { params })
  },

  getStats() {
    return axios.get<{ data: DeliveryStats }>(`${baseUrl}/stats`)
  },

  getById(id: number) {
    return axios.get<{ data: DeliveryOrder }>(`${baseUrl}/order/${id}`)
  },

  ship(data: ShipRequest) {
    return axios.post(`${baseUrl}/ship`, data)
  },

  complete(id: number) {
    return axios.post(`${baseUrl}/complete/${id}`)
  }
}
