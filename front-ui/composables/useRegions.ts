// 中国省市县三级数据（从 GitHub 官方数据源按需加载）
// 原始数据: https://github.com/modood/Administrative-divisions-of-China
// 缓存策略: 首次使用后缓存到内存，不重复请求

export interface RegionItem {
  code: string
  name: string
}

// 省/市/区 的层级结构（用于下拉联动）
export interface RegionTreeNode {
  name: string
  code: string
  children?: RegionTreeNode[]
}

// 扁平数据（源数据格式）
let provincesCache: RegionItem[] | null = null
let citiesCache: RegionItem[] & { provinceCode: string }[] | null = null
let districtsCache: RegionItem[] & { cityCode: string }[] | null = null

const BASE_URL = 'https://raw.githubusercontent.com/modood/Administrative-divisions-of-China/master'

async function fetchJson<T>(path: string): Promise<T> {
  const res = await fetch(`${BASE_URL}/${path}`, { cache: 'force-cache' })
  if (!res.ok) throw new Error(`加载区域数据失败: ${res.status}`)
  return res.json()
}

export async function getProvinces(): Promise<RegionItem[]> {
  if (provincesCache) return provincesCache
  const data = await fetchJson<{ code: string; name: string }[]>('dist/provinces.json')
  provincesCache = data.map(p => ({ code: p.code, name: p.name }))
  return provincesCache
}

export async function getCities(): Promise<(RegionItem & { provinceCode: string })[]> {
  if (citiesCache) return citiesCache
  const data = await fetchJson<{ code: string; name: string; provinceCode: string }[]>('dist/cities.json')
  citiesCache = data.map(c => ({ code: c.code, name: c.name, provinceCode: c.provinceCode }))
  return citiesCache
}

export async function getDistricts(): Promise<(RegionItem & { cityCode: string })[]> {
  if (districtsCache) return districtsCache
  const data = await fetchJson<{ code: string; name: string; cityCode: string }[]>('dist/areas.json')
  districtsCache = data.map(d => ({ code: d.code, name: d.name, cityCode: d.cityCode }))
  return districtsCache
}

// 省列表
export async function getAllProvinces(): Promise<RegionItem[]> {
  return getProvinces()
}

// 根据省名称查 code
function findProvinceCode(provinces: RegionItem[], name: string): string {
  const p = provinces.find(p => p.name === name)
  return p ? p.code : ''
}

// 根据省名称获取城市列表
export async function getCitiesByProvince(provinceName: string): Promise<RegionItem[]> {
  const [provinces, cities] = await Promise.all([getProvinces(), getCities()])
  const provinceCode = findProvinceCode(provinces, provinceName)
  return provinceCode ? cities.filter(c => c.provinceCode === provinceCode).map(c => ({ code: c.code, name: c.name })) : []
}

// 根据省市名称获取区县列表
export async function getDistrictsByCity(provinceName: string, cityName: string): Promise<RegionItem[]> {
  const [provinces, cities, districts] = await Promise.all([getProvinces(), getCities(), getDistricts()])
  const provinceCode = findProvinceCode(provinces, provinceName)
  const city = cities.find(c => c.provinceCode === provinceCode && c.name === cityName)
  return city ? districts.filter(d => d.cityCode === city.code).map(d => ({ code: d.code, name: d.name })) : []
}
