import http from './http'

export const authApi = {
  register: (data) => http.post('/api/auth/register', data),
  login: (data) => http.post('/api/auth/login', data),
  me: () => http.get('/api/users/me')
}

export const productApi = {
  page: (params) => http.get('/api/products', { params }),
  detail: (id) => http.get(`/api/products/${id}`),
  stock: (skuId) => http.get(`/api/inventory/${skuId}`)
}

export const userApi = {
  addresses: () => http.get('/api/users/me/addresses'),
  addAddress: (data) => http.post('/api/users/me/addresses', data)
}

export const cartApi = {
  list: () => http.get('/api/cart'),
  add: (data) => http.post('/api/cart', data),
  remove: (skuId) => http.delete(`/api/cart/${skuId}`)
}

export const orderApi = {
  create: (data = {}) => http.post('/api/orders', data),
  list: (params) => http.get('/api/orders', { params }),
  get: (orderNo) => http.get(`/api/orders/${orderNo}`),
  cancel: (orderNo) => http.post(`/api/orders/${orderNo}/cancel`)
}

export const paymentApi = {
  pay: (orderNo) => http.post(`/api/payments/${orderNo}/pay`)
}
