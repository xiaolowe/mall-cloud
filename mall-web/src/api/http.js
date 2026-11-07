import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: '/', timeout: 10000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('mall_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

function clearSession() {
  localStorage.removeItem('mall_token')
  localStorage.removeItem('mall_user')
}

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body.code === 'number') {
      if (body.code === 200) {
        return body.data
      }
      if (body.code === 401) {
        clearSession()
        window.location.hash = '#/login'
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (error) => {
    if (error.response?.status === 401) {
      clearSession()
      window.location.hash = '#/login'
      ElMessage.error('登录已失效，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default http
