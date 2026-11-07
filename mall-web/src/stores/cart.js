import { defineStore } from 'pinia'
import { cartApi } from '../api/mall'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false
  }),
  getters: {
    count: (state) => state.items.reduce((sum, item) => sum + item.quantity, 0),
    total: (state) => state.items.reduce((sum, item) => sum + Number(item.subtotal), 0)
  },
  actions: {
    async load() {
      if (!localStorage.getItem('mall_token')) {
        this.items = []
        return
      }
      this.loading = true
      try {
        this.items = await cartApi.list()
      } finally {
        this.loading = false
      }
    },
    async add(skuId, quantity = 1) {
      await cartApi.add({ skuId, quantity })
      await this.load()
    },
    async remove(skuId) {
      await cartApi.remove(skuId)
      await this.load()
    },
    clear() {
      this.items = []
    }
  }
})
