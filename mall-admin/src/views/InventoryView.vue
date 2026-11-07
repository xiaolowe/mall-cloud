<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { inventoryApi } from '../api/admin'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 20 })

async function load() {
  loading.value = true
  try {
    const data = await inventoryApi.page({ page: query.page, size: query.size })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function adjust(row, sign) {
  const { value } = await ElMessageBox.prompt(
    `${sign > 0 ? '补货' : '扣减'} SKU #${row.skuId} 的数量`, '调整库存',
    { inputPattern: /^[1-9]\d*$/, inputErrorMessage: '请输入正整数' }
  )
  const delta = sign * Number(value)
  await inventoryApi.adjust(row.skuId, delta)
  ElMessage.success('库存已调整')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <span>库存按 SKU 维度管理</span>
      <span class="spacer"></span>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="skuId" label="SKU ID" width="100" />
      <el-table-column prop="total" label="总库存" width="120" />
      <el-table-column prop="available" label="可售库存" width="120" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="adjust(row, 1)">补货</el-button>
          <el-button link type="warning" @click="adjust(row, -1)">扣减</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination layout="total, prev, pager, next" :total="total"
                     :page-size="query.size" :current-page="query.page"
                     @current-change="(p) => { query.page = p; load() }" />
    </div>
  </div>
</template>
