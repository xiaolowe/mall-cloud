<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '../api/admin'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, status: null, orderNo: '' })

const detailVisible = ref(false)
const detail = ref(null)

const statusMap = {
  0: { text: '待支付', type: 'warning' },
  1: { text: '已支付', type: 'success' },
  2: { text: '已取消', type: 'info' },
  3: { text: '已完成', type: 'success' }
}

function money(v) {
  return '¥' + Number(v || 0).toFixed(2)
}

async function load() {
  loading.value = true
  try {
    const data = await orderApi.page({
      page: query.page, size: query.size,
      status: query.status ?? undefined,
      orderNo: query.orderNo || undefined
    })
    list.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  load()
}

async function view(row) {
  detail.value = await orderApi.get(row.orderNo)
  detailVisible.value = true
}

async function ship(row) {
  await ElMessageBox.confirm(`确认发货订单 ${row.orderNo}？`, '发货', { type: 'warning' })
  await orderApi.ship(row.orderNo)
  ElMessage.success('已发货')
  await load()
}

async function close(row) {
  await ElMessageBox.confirm(`关闭订单 ${row.orderNo}？库存将回补。`, '关闭订单', { type: 'warning' })
  await orderApi.close(row.orderNo)
  ElMessage.success('已关闭')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.orderNo" placeholder="订单号" clearable style="width: 220px" @keyup.enter="search" />
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 140px">
        <el-option label="待支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="已取消" :value="2" />
        <el-option label="已完成" :value="3" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
      <span class="spacer"></span>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column label="金额" width="120">
        <template #default="{ row }">{{ money(row.totalAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusMap[row.status]?.type" size="small">{{ statusMap[row.status]?.text }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receiverName" label="收货人" width="100" />
      <el-table-column prop="createdAt" label="下单时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="view(row)">详情</el-button>
          <el-button v-if="row.status === 1" link type="success" @click="ship(row)">发货</el-button>
          <el-button v-if="row.status === 0" link type="danger" @click="close(row)">关闭</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination layout="total, prev, pager, next" :total="total"
                     :page-size="query.size" :current-page="query.page"
                     @current-change="(p) => { query.page = p; load() }" />
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="560px">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ statusMap[detail.status]?.text }}</el-descriptions-item>
          <el-descriptions-item label="金额">{{ money(detail.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detail.receiverName }} {{ detail.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="地址">{{ detail.receiverAddress }}</el-descriptions-item>
        </el-descriptions>
        <el-table :data="detail.items" border size="small" style="margin-top: 14px">
          <el-table-column prop="productName" label="商品" />
          <el-table-column prop="skuTitle" label="规格" />
          <el-table-column label="单价" width="90">
            <template #default="{ row }">{{ money(row.price) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="70" />
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>
