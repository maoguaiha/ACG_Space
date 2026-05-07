/**
 * V2.0 数字资产系统 API
 * 包含：抽赏、背包、跳蚤市场、地址管理
 */

import type { PageResult } from './useApi'

export interface GachaPool {
  id: number
  name: string
  description: string
  banner: string
  rarity: string
  singleCost: number
  tenCost: number
  status: number
  startTime: string
  endTime: string
  totalStock: number
  remainingStock: number
}

export interface GachaResult {
  records: GachaRecord[]
  isGuaranteed: boolean
  guaranteedRarity?: string
}

export interface GachaRecord {
  id: number
  poolId: number
  poolName: string
  itemId: number
  itemName: string
  itemImage: string
  itemRarity: string
  itemType: string
  assetId?: number
  createTime: string
}

export interface UserAsset {
  id: number
  assetKey: string
  userId: number
  itemId: number
  itemName: string
  itemImage: string
  itemRarity: string
  itemType: string
  quantity: number
  status: number
  createTime: string
  acquireType?: string
}

export interface MarketItem {
  id: number
  assetId: number
  itemName: string
  itemImage: string
  itemRarity: string
  itemType: string
  sellerId: number
  sellerName: string
  sellerAvatar: string
  price: number
  status: number
  createTime: string
}

export interface UserAddress {
  id: number
  userId: number
  receiver: string
  phone: string
  province: string
  city: string
  district: string
  detailAddress: string
  postalCode?: string
  isDefault: boolean
  createTime: string
}

function getBaseUrl(): string {
  const config = useRuntimeConfig()
  if (import.meta.server) {
    return `${config.apiInternalBase}/api`
  }
  return config.public.apiBase
}

async function v2Fetch<T>(
  path: string,
  options?: Parameters<typeof $fetch>[1]
): Promise<T> {
  const baseUrl = getBaseUrl()
  const result = await $fetch<{ code: number; msg: string; data: T }>(`${baseUrl}${path}`, {
    ...options,
    headers: {
      ...options?.headers
    }
  })
  if (result.code !== 200) {
    throw new Error(result.msg || '请求失败')
  }
  return result.data
}

export const gachaApi = {
  fetchActivePools() {
    return v2Fetch<GachaPool[]>('/gacha/active')
  },
  fetchPoolDetail(poolId: number) {
    return v2Fetch<GachaPool>(`/gacha/${poolId}`)
  },
  draw(poolId: number, count: number) {
    return v2Fetch<GachaResult>('/gacha/draw', {
      method: 'POST',
      body: JSON.stringify({ poolId, count })
    })
  },
  fetchRecords(pageNum = 1, pageSize = 20) {
    return v2Fetch<PageResult<GachaRecord>>(`/gacha/records?pageNum=${pageNum}&pageSize=${pageSize}`)
  },
  fetchUserPoints() {
    return v2Fetch<number>('/points/my')
  }
}

export const assetApi = {
  fetchUserAssets(pageNum = 1, pageSize = 20) {
    return v2Fetch<PageResult<UserAsset>>(`/asset/page?pageNum=${pageNum}&pageSize=${pageSize}`)
  },
  fetchDetail(assetId: number) {
    return v2Fetch<UserAsset>(`/asset/${assetId}`)
  }
}

export const marketApi = {
  fetchItems(params: {
    pageNum?: number
    pageSize?: number
    itemName?: string
    itemType?: string
    rarity?: string
    minPrice?: number
    maxPrice?: number
    sortBy?: string
  }) {
    const query = new URLSearchParams()
    if (params.pageNum) query.set('pageNum', String(params.pageNum))
    if (params.pageSize) query.set('pageSize', String(params.pageSize))
    if (params.itemName) query.set('itemName', params.itemName)
    if (params.itemType) query.set('itemType', params.itemType)
    if (params.rarity) query.set('rarity', params.rarity)
    if (params.minPrice) query.set('minPrice', String(params.minPrice))
    if (params.maxPrice) query.set('maxPrice', String(params.maxPrice))
    if (params.sortBy) query.set('sortBy', params.sortBy)
    return v2Fetch<PageResult<MarketItem>>(`/market/page?${query.toString()}`)
  },
  fetchItemDetail(itemId: number) {
    return v2Fetch<MarketItem>(`/market/${itemId}`)
  },
  buy(itemId: number) {
    return v2Fetch<{ orderId: string }>('/market/buy', {
      method: 'POST',
      body: JSON.stringify({ itemId })
    })
  },
  listAsset(assetId: number, price: number) {
    return v2Fetch<{ itemId: number }>('/market/list', {
      method: 'POST',
      body: JSON.stringify({ assetId, price })
    })
  }
}

export const addressApi = {
  fetchList() {
    return v2Fetch<UserAddress[]>('/address/list')
  },
  create(data: {
    receiver: string
    phone: string
    province: string
    city: string
    district: string
    detailAddress: string
    postalCode?: string
    isDefault?: boolean
  }) {
    return v2Fetch<number>('/address', {
      method: 'POST',
      body: JSON.stringify(data)
    })
  },
  update(id: number, data: {
    receiver?: string
    phone?: string
    province?: string
    city?: string
    district?: string
    detailAddress?: string
    postalCode?: string
    isDefault?: boolean
  }) {
    return v2Fetch<boolean>('/address', {
      method: 'PUT',
      body: JSON.stringify({ id, ...data })
    })
  },
  delete(id: number) {
    return v2Fetch<boolean>(`/address/${id}`, { method: 'DELETE' })
  },
  setDefault(id: number) {
    return v2Fetch<boolean>(`/address/${id}/default`, { method: 'PUT' })
  }
}

export interface SynthesizeRecipe {
  id: number
  name: string
  description: string
  resultItemId: number
  resultItemName: string
  resultItemImage?: string
  resultItemRarity?: string
  resultQuantity: number
  costType: string
  costItems: SynthesizeCostItem[]
  costPoints: number
  successRate: number
  status: number
}

export interface SynthesizeCostItem {
  itemId: number
  itemName: string
  itemImage?: string
  count: number
  owned: number
}

export interface SynthesizeResult {
  success: boolean
  message: string
  assetId?: number
  itemName?: string
  itemImage?: string
  itemRarity?: string
}

export interface SynthesizeRecord {
  id: number
  recipeId: number
  recipeName: string
  resultItemId: number
  resultItemName: string
  resultQuantity: number
  costPoints: number
  success: boolean
  status: number
  createTime: string
}

export const synthesizeApi = {
  fetchRecipes(pageNum = 1, pageSize = 20, name?: string) {
    let url = `/synthesize/recipes?pageNum=${pageNum}&pageSize=${pageSize}`
    if (name) url += `&name=${encodeURIComponent(name)}`
    return v2Fetch<PageResult<SynthesizeRecipe>>(url)
  },
  do(recipeId: number) {
    return v2Fetch<SynthesizeResult>('/synthesize/do', {
      method: 'POST',
      body: JSON.stringify({ recipeId })
    })
  },
  fetchRecords(pageNum = 1, pageSize = 20) {
    return v2Fetch<PageResult<SynthesizeRecord>>(`/synthesize/records?pageNum=${pageNum}&pageSize=${pageSize}`)
  }
}
