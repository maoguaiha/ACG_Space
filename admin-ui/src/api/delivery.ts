import request from './request'

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
    return request.get<{ data: DeliveryPageResult }>('/admin/redeem/orders', { params })
  },

  getStats() {
    return request.get<{ data: DeliveryStats }>('/admin/redeem/stats')
  },

  getById(id: number) {
    return request.get<{ data: DeliveryOrder }>('/admin/redeem/order/${id}')
  },

  ship(data: ShipRequest) {
    return request.post('/admin/redeem/ship', data)
  },

  complete(id: number) {
    return request.post('/admin/redeem/complete/${id}')
  }
}
