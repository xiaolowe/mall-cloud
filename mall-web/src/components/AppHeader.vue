<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { useUiStore } from '../stores/ui'

const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const ui = useUiStore()

function logout() {
  auth.logout()
  cart.clear()
  ui.cartOpen = false
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link to="/" class="brand">Mall<span>Cloud</span></router-link>
      <nav class="nav">
        <router-link to="/" class="nav-link">商城</router-link>
        <router-link to="/orders" class="nav-link">我的订单</router-link>
      </nav>
      <div class="header-right">
        <el-badge :value="cart.count" :max="99" :hidden="cart.count === 0">
          <el-button circle :icon="ShoppingCart" @click="ui.cartOpen = true" />
        </el-badge>
        <template v-if="auth.token">
          <span class="user-name">{{ auth.user?.nickname || auth.user?.username }}</span>
          <el-button text @click="logout">退出</el-button>
        </template>
        <el-button v-else type="primary" @click="router.push('/login')">登录</el-button>
      </div>
    </div>
  </header>
</template>
