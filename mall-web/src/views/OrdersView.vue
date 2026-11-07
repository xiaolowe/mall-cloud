<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi, paymentApi } from '../api/mall'

const orders = ref([])
const loading = ref(false)
const paying = ref('')

const statusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'success' },
  2: { text: '已取消', type: 'info' },
  3: { text: '已完成', type: 'success' }
}

async function loadOrders() {
  loading.value = true
  try {
    const page = await orderApi.list({ page: 1, size: 20 })
    orders.value = page.records || []
  } finally {
    loading.value = false
  }
}

async function pay(order) {
  paying.value = order.orderNo
  try {
    await paymentApi.pay(order.orderNo)
    ElMessage.success('支付成功')
    await loadOrders()
  } finally {
    paying.value = ''
  }
}

async function cancel(order) {
  try {
    await ElMessageBox.confirm('取消后库存会回补，确定取消该订单吗？', '取消订单', {
      type: 'warning',
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想'
    })
  } catch (e) {
    return
  }
  await orderApi.cancel(order.orderNo)
  ElMessage.success('订单已取消')
  await loadOrders()
}

onMounted(loadOrders)
</script>

<template>
  <div>
    <div class="section-title">
      <h2>我的订单</h2>
      <el-button text @click="loadOrders">刷新</el-button>
    </div>

    <div v-loading="loading">
      <el-empty v-if="!loading && !orders.length" description="暂无订单" />

      <div v-for="order in orders" :key="order.orderNo" class="order-card">
        <div class="order-head">
          <span class="order-no">{{ order.orderNo }}</span>
          <el-tag :type="statusMap[order.status]?.type" size="small">
            {{ statusMap[order.status]?.text || order.statusText }}
          </el-tag>
          <span class="order-amount">¥{{ Number(order.totalAmount).toFixed(2) }}</span>
        </div>

        <div class="order-items">
          <div v-for="item in order.items" :key="item.skuId" class="order-item">
            <img
              :src="item.coverUrl"
              alt=""
              @error="(e) => (e.target.style.visibility = 'hidden')"
            >
            <div style="flex: 1">
              <div>{{ item.productName }}</div>
              <div class="cart-sku">{{ item.skuTitle }} × {{ item.quantity }}</div>
            </div>
            <div class="cart-price">¥{{ Number(item.price).toFixed(2) }}</div>
          </div>
        </div>

        <div v-if="order.status === 0" class="order-actions">
          <el-button
            type="primary"
            :loading="paying === order.orderNo"
            @click="pay(order)"
          >
            模拟支付
          </el-button>
          <el-button type="danger" plain @click="cancel(order)">取消订单</el-button>
        </div>
      </div>
    </div>
  </div>
</template>
