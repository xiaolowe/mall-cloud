<script setup>
import { onMounted } from 'vue'
import AppHeader from './components/AppHeader.vue'
import CartDrawer from './components/CartDrawer.vue'
import AddressDialog from './components/AddressDialog.vue'
import { useAuthStore } from './stores/auth'
import { useCartStore } from './stores/cart'

const auth = useAuthStore()
const cart = useCartStore()

onMounted(async () => {
  if (!auth.token) return
  try {
    await auth.fetchMe()
    await cart.load()
  } catch (e) {
    // http interceptor already reported the problem
  }
})
</script>

<template>
  <div class="app-shell">
    <AppHeader />
    <main class="page">
      <router-view />
    </main>
    <CartDrawer />
    <AddressDialog />
  </div>
</template>
