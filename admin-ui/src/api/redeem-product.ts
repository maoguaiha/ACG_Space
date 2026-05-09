import axios from 'axios'

const baseUrl = '/api'

export function listProducts(params: any) {
  return axios.get(`${baseUrl}/admin/redeem-product/list`, { params })
}

export function getProduct(id: number) {
  return axios.get(`${baseUrl}/admin/redeem-product/${id}`)
}

export function createProduct(data: any) {
  return axios.post(`${baseUrl}/admin/redeem-product/create`, data)
}

export function updateProduct(data: any) {
  return axios.put(`${baseUrl}/admin/redeem-product/update`, data)
}

export function deleteProduct(id: number) {
  return axios.delete(`${baseUrl}/admin/redeem-product/${id}`)
}
