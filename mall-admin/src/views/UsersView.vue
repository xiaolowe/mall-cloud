<script setup>
import { onMounted, reactive, ref } from 'vue'
import { userApi } from '../api/admin'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 10, keyword: '' })

async function load() {
  loading.value = true
  try {
    const data = await userApi.page({ page: query.page, size: query.size, keyword: query.keyword || undefined })
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

onMounted(load)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="用户名 / 昵称" clearable style="width: 240px"
                @keyup.enter="search" />
      <el-button type="primary" @click="search">搜索</el-button>
      <span class="spacer"></span>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" />
    </el-table>

    <div class="pager">
      <el-pagination layout="total, prev, pager, next" :total="total"
                     :page-size="query.size" :current-page="query.page"
                     @current-change="(p) => { query.page = p; load() }" />
    </div>
  </div>
</template>
