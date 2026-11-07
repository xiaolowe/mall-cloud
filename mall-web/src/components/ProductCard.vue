<script setup>
defineProps({
  product: { type: Object, required: true },
  loading: { type: Boolean, default: false }
})

defineEmits(['add'])
</script>

<template>
  <div class="product-card">
    <div class="product-cover">
      <el-image :src="product.coverUrl" fit="cover" lazy>
        <template #error>
          <div class="cover-fallback">{{ product.name?.slice(0, 2) }}</div>
        </template>
      </el-image>
      <span class="product-tag">现货</span>
    </div>
    <div class="product-body">
      <div class="product-name">{{ product.name }}</div>
      <div class="product-sub">{{ product.subtitle }}</div>
      <div class="product-foot">
        <div class="product-price">¥{{ Number(product.price).toFixed(2) }}</div>
        <el-button type="primary" size="small" :loading="loading" @click="$emit('add', product)">
          加入购物车
        </el-button>
      </div>
    </div>
  </div>
</template>
