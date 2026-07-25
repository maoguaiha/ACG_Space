import request from './request'

export function listProducts(params: any) {
  return request.get('/admin/redeem-product/list', { params })
}

export function getProduct(id: number) {
  return request.get('/admin/redeem-product/${id}')
}

export function createProduct(data: any) {
  return request.post('/admin/redeem-product/create', data)
}

export function updateProduct(data: any) {
  return request.put('/admin/redeem-product/update', data)
}

export function deleteProduct(id: number) {
  return request.delete('/admin/redeem-product/${id}')
}
