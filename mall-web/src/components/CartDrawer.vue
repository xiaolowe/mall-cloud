<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '../stores/cart'
import { useUiStore } from '../stores/ui'
import { orderApi, userApi } from '../api/mall'

const router = useRouter()
const cart = useCartStore()
const ui = useUiStore()

async function checkout() {
  if (!cart.items.length) return
  const addresses = await userApi.addresses()
  if (!addresses.length) {
    ElMessage.warning('请先添加收货地址')
    ui.cartOpen = false
    ui.addressOpen = true
    return
  }
  const order = await orderApi.create({})
  ElMessage.success(`下单成功：${order.orderNo}`)
  await cart.load()
  ui.cartOpen = false
  router.push('/orders')
}

async function remove(skuId) {
  await cart.remove(skuId)
  ElMessage.success('已移除')
}
</script>

<template>
  <el-drawer v-model="ui.cartOpen" title="购物车" size="420px">
    <div v-if="cart.items.length">
      <div v-for="item in cart.items" :key="item.skuId" class="cart-item">
        <img
          class="cart-thumb"
          :src="item.coverUrl"
          alt=""
          @error="(e) => (e.target.style.visibility = 'hidden')"
        >
        <div class="cart-info">
          <div class="cart-name">{{ item.productName }}</div>
          <div class="cart-sku">{{ item.skuTitle }} × {{ item.quantity }}</div>
          <div class="cart-price" style="margin-top: 6px">¥{{ Number(item.subtotal).toFixed(2) }}</div>
        </div>
        <el-button text type="danger" size="small" @click="remove(item.skuId)">移除</el-button>
      </div>
    </div>
    <el-empty v-else description="购物车还是空的" />

    <template #footer>
      <div class="cart-footer">
        <span>合计</span>
        <span class="cart-total">¥{{ cart.total.toFixed(2) }}</span>
      </div>
      <el-button type="primary" style="width: 100%" :disabled="!cart.items.length" @click="checkout">
        结算下单
      </el-button>
    </template>
  </el-drawer>
</template>
