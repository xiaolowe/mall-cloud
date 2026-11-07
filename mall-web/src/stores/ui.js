import { defineStore } from 'pinia'

export const useUiStore = defineStore('ui', {
  state: () => ({
    cartOpen: false,
    addressOpen: false
  })
})
