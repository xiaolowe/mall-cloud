import http from './http'

export const categoryApi = {
  list: () => http.get('/api/admin/categories'),
  create: (data) => http.post('/api/admin/categories', data),
  update: (id, data) => http.put(`/api/admin/categories/${id}`, data),
  remove: (id) => http.delete(`/api/admin/categories/${id}`)
}

export const productApi = {
  page: (params) => http.get('/api/admin/products', { params }),
  get: (id) => http.get(`/api/admin/products/${id}`),
  create: (data) => http.post('/api/admin/products', data),
  update: (id, data) => http.put(`/api/admin/products/${id}`, data),
  setStatus: (id, status) => http.put(`/api/admin/products/${id}/status`, null, { params: { status } }),
  remove: (id) => http.delete(`/api/admin/products/${id}`),
  skus: (productId) => http.get(`/api/admin/products/${productId}/skus`),
  createSku: (data) => http.post('/api/admin/skus', data),
  updateSku: (id, data) => http.put(`/api/admin/skus/${id}`, data),
  removeSku: (id) => http.delete(`/api/admin/skus/${id}`)
}

export const inventoryApi = {
  page: (params) => http.get('/api/admin/inventory', { params }),
  adjust: (skuId, delta) => http.post(`/api/admin/inventory/${skuId}/adjust`, null, { params: { delta } })
}

export const orderApi = {
  page: (params) => http.get('/api/admin/orders', { params }),
  get: (orderNo) => http.get(`/api/admin/orders/${orderNo}`),
  stats: () => http.get('/api/admin/orders/stats'),
  ship: (orderNo) => http.post(`/api/admin/orders/${orderNo}/ship`),
  close: (orderNo) => http.post(`/api/admin/orders/${orderNo}/close`)
}

export const userApi = {
  page: (params) => http.get('/api/admin/users', { params }),
  stats: () => http.get('/api/admin/stats/users')
}
