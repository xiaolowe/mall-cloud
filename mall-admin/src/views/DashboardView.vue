<script setup>
import { onMounted, ref } from 'vue'
import { orderApi, userApi, productApi } from '../api/admin'

const stats = ref({})
const userTotal = ref(0)
const productTotal = ref(0)

function money(v) {
  return '¥' + Number(v || 0).toFixed(2)
}

onMounted(async () => {
  const [orderStats, userStats, productPage] = await Promise.all([
    orderApi.stats().catch(() => ({})),
    userApi.stats().catch(() => ({ totalUsers: 0 })),
    productApi.page({ page: 1, size: 1 }).catch(() => ({ total: 0 }))
  ])
  stats.value = orderStats || {}
  userTotal.value = userStats?.totalUsers || 0
  productTotal.value = productPage?.total || 0
})
</script>

<template>
  <div>
    <div class="stat-grid">
      <div class="stat-card">
        <div class="label">销售额（已支付+已完成）</div>
        <div class="value brand">{{ money(stats.salesAmount) }}</div>
      </div>
      <div class="stat-card">
        <div class="label">订单总数</div>
        <div class="value">{{ stats.totalOrders || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">待支付</div>
        <div class="value">{{ stats.pendingPayment || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已支付</div>
        <div class="value">{{ stats.paid || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已完成</div>
        <div class="value">{{ stats.completed || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已取消</div>
        <div class="value">{{ stats.cancelled || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="label">商品数</div>
        <div class="value">{{ productTotal }}</div>
      </div>
      <div class="stat-card">
        <div class="label">用户数</div>
        <div class="value">{{ userTotal }}</div>
      </div>
    </div>
    <el-alert
      title="这是演示用管理后台，/api/admin/** 暂未启用鉴权，正式上线前需加管理员登录与权限校验。"
      type="info" :closable="false" show-icon />
  </div>
</template>
