import axios from 'axios'

const baseUrl = '/api'

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
    return axios.get<{ data: TransactionPageResult }>(`${baseUrl}/transaction/page`, { params })
  },

  getStats() {
    return axios.get<{ data: TransactionStats }>(`${baseUrl}/transaction/stats`)
  },

  getByOrderId(orderId: string) {
    return axios.get<{ data: Transaction }>(`${baseUrl}/transaction/${orderId}`)
  },

  compensate(orderId: string) {
    return axios.post(`${baseUrl}/transaction/${orderId}/compensate`)
  },

  retry(orderId: string) {
    return axios.post(`${baseUrl}/transaction/${orderId}/retry`)
  }
}
