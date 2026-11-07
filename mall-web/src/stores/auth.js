import { defineStore } from 'pinia'
import { authApi } from '../api/mall'

function readUser() {
  try {
    return JSON.parse(localStorage.getItem('mall_user') || 'null')
  } catch (e) {
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('mall_token') || '',
    user: readUser()
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },
  actions: {
    setSession(data) {
      this.token = data.token
      this.user = data.user
      localStorage.setItem('mall_token', data.token)
      localStorage.setItem('mall_user', JSON.stringify(data.user))
    },
    async login(form) {
      const data = await authApi.login(form)
      this.setSession(data)
      return data
    },
    async register(form) {
      const data = await authApi.register(form)
      this.setSession(data)
      return data
    },
    async fetchMe() {
      if (!this.token) return null
      const user = await authApi.me()
      this.user = user
      localStorage.setItem('mall_user', JSON.stringify(user))
      return user
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('mall_token')
      localStorage.removeItem('mall_user')
    }
  }
})
