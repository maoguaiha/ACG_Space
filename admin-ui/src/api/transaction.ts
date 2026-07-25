import request from './request'

export interface Transaction {
  id?: number
  orderId: string
  buyerId: number
  sellerId: number
  assetId: number
  itemId: number
  itemName: string
  itemImage?: string
  itemRarity?: string
  amount: number
  fee?: number
  sellerAmount?: number
  status?: number | string
  buyerName?: string
  sellerName?: string
  transactionId?: string
  errorMsg?: string
  rocketmqTxId?: string
  completeTime?: string
  createTime?: string
  updateTime?: string
}

export interface TransactionPageResult {
  records: Transaction[]
  total: number
  size: number
  current: number
}

export interface TransactionStats {
  todayAmount: number
  todayCount: number
  pendingCount: number
  successRate: number
  amount?: number
  successCount?: number
  errorCount?: number
}

export const transactionApi = {
  page(params: {
    pageNum: number
    pageSize: number
    orderId?: string
    status?: number
  }) {
    return request.get<{ data: TransactionPageResult }>('/transaction/page', { params })
  },

  getStats() {
    return request.get<{ data: TransactionStats }>('/transaction/stats')
  },

  getByOrderId(orderId: string) {
    return request.get<{ data: Transaction }>('/transaction/${orderId}')
  },

  compensate(orderId: string) {
    return request.post('/transaction/${orderId}/compensate')
  },

  retry(orderId: string) {
    return request.post('/transaction/${orderId}/retry')
  }
}
