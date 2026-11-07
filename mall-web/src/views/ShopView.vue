<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '../components/ProductCard.vue'
import { productApi } from '../api/mall'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'
import { useUiStore } from '../stores/ui'

const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()
const ui = useUiStore()

const products = ref([])
const loading = ref(false)
const addingId = ref(null)

async function loadProducts() {
  loading.value = true
  try {
    const page = await productApi.page({ page: 1, size: 20 })
    products.value = page.records || []
  } finally {
    loading.value = false
  }
}

async function addToCart(product) {
  if (!auth.token) {
    ElMessage.warning('请先登录')
    router.push({ name: 'login', query: { redirect: '/' } })
    return
  }
  addingId.value = product.id
  try {
    const detail = await productApi.detail(product.id)
    const sku = detail.skus?.[0]
    if (!sku) {
      ElMessage.warning('该商品暂无规格')
      return
    }
    await cart.add(sku.id, 1)
    ElMessage.success('已加入购物车')
    ui.cartOpen = true
  } finally {
    addingId.value = null
  }
}

onMounted(loadProducts)
</script>

<template>
  <div>
    <section class="hero">
      <h1>Mall Cloud 微服务商城</h1>
      <p>Spring Cloud Alibaba · Vue 3 · 下单 / 库存 / 支付完整链路</p>
    </section>

    <div class="section-title">
      <h2>全部商品</h2>
      <span class="muted">{{ products.length }} 件在售</span>
    </div>

    <div v-if="loading" class="product-grid">
      <el-skeleton v-for="i in 6" :key="i" animated>
        <template #template>
          <el-skeleton-item variant="image" style="height: 160px; border-radius: 14px" />
          <div style="padding: 14px">
            <el-skeleton-item variant="h3" style="width: 60%" />
            <el-skeleton-item variant="text" style="margin-top: 10px" />
          </div>
        </template>
      </el-skeleton>
    </div>

    <div v-else class="product-grid">
      <ProductCard
        v-for="product in products"
        :key="product.id"
        :product="product"
        :loading="addingId === product.id"
        @add="addToCart"
      />
    </div>
  </div>
</template>
