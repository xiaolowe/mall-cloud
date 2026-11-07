<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { productApi, categoryApi } from '../api/admin'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const categories = ref([])
const query = reactive({ page: 1, size: 10, keyword: '', categoryId: null, status: null })

const dialogVisible = ref(false)
const editing = ref(null)
const form = reactive({ categoryId: null, name: '', subtitle: '', coverUrl: '', description: '', price: 0, status: 1 })

const skuVisible = ref(false)
const skuProduct = ref(null)
const skus = ref([])
const skuForm = reactive({ id: null, productId: null, title: '', price: 0, specJson: '', initialStock: 0 })
const skuEditing = ref(false)

function money(v) {
  return '¥' + Number(v || 0).toFixed(2)
}

function categoryName(id) {
  return categories.value.find((c) => c.id === id)?.name || id
}

async function load() {
  loading.value = true
  try {
    const data = await productApi.page({
      page: query.page, size: query.size,
      keyword: query.keyword || undefined,
      categoryId: query.categoryId ?? undefined,
      status: query.status ?? undefined
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

function openCreate() {
  editing.value = null
  Object.assign(form, { categoryId: categories.value[0]?.id ?? null, name: '', subtitle: '', coverUrl: '', description: '', price: 0, status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = row
  Object.assign(form, {
    categoryId: row.categoryId, name: row.name, subtitle: row.subtitle,
    coverUrl: row.coverUrl, description: row.description, price: row.price, status: row.status
  })
  dialogVisible.value = true
}

async function save() {
  if (!form.name.trim() || !form.categoryId) {
    ElMessage.warning('请填写分类和商品名称')
    return
  }
  const payload = {
    categoryId: form.categoryId, name: form.name, subtitle: form.subtitle,
    coverUrl: form.coverUrl, description: form.description, price: form.price, status: form.status
  }
  if (editing.value) {
    await productApi.update(editing.value.id, payload)
  } else {
    await productApi.create(payload)
  }
  ElMessage.success('已保存')
  dialogVisible.value = false
  await load()
}

async function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  await productApi.setStatus(row.id, next)
  ElMessage.success(next === 1 ? '已上架' : '已下架')
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`删除商品「${row.name}」及其 SKU？`, '提示', { type: 'warning' })
  await productApi.remove(row.id)
  ElMessage.success('已删除')
  await load()
}

async function openSkus(row) {
  skuProduct.value = row
  skus.value = await productApi.skus(row.id)
  skuVisible.value = true
}

function openSkuCreate() {
  skuEditing.value = false
  Object.assign(skuForm, { id: null, productId: skuProduct.value.id, title: '', price: skuProduct.value.price, specJson: '', initialStock: 0 })
}

function openSkuEdit(row) {
  skuEditing.value = true
  Object.assign(skuForm, { id: row.id, productId: row.productId, title: row.title, price: row.price, specJson: row.specJson, initialStock: 0 })
}

async function saveSku() {
  if (!skuForm.title.trim()) {
    ElMessage.warning('请输入 SKU 标题')
    return
  }
  const payload = { productId: skuForm.productId, title: skuForm.title, price: skuForm.price, specJson: skuForm.specJson, initialStock: skuForm.initialStock }
  if (skuEditing.value) {
    await productApi.updateSku(skuForm.id, payload)
  } else {
    await productApi.createSku(payload)
  }
  ElMessage.success('已保存 SKU')
  skus.value = await productApi.skus(skuProduct.value.id)
  Object.assign(skuForm, { id: null, title: '', specJson: '', initialStock: 0 })
}

async function removeSku(row) {
  await ElMessageBox.confirm(`删除 SKU「${row.title}」？`, '提示', { type: 'warning' })
  await productApi.removeSku(row.id)
  ElMessage.success('已删除')
  skus.value = await productApi.skus(skuProduct.value.id)
}

onMounted(async () => {
  categories.value = await categoryApi.list().catch(() => [])
  await load()
})
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="商品名称" clearable style="width: 200px" @keyup.enter="search" />
      <el-select v-model="query.categoryId" placeholder="全部分类" clearable style="width: 150px">
        <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px">
        <el-option label="已上架" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
      <span class="spacer"></span>
      <el-button type="primary" @click="openCreate">新增商品</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <el-image v-if="row.coverUrl" :src="row.coverUrl" class="cover-sm" fit="cover" />
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column label="分类" width="120">
        <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
      </el-table-column>
      <el-table-column label="价格" width="110">
        <template #default="{ row }">{{ money(row.price) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已上架' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="openSkus(row)">SKU</el-button>
          <el-button link type="warning" @click="toggleStatus(row)">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination layout="total, prev, pager, next" :total="total"
                     :page-size="query.size" :current-page="query.page"
                     @current-change="(p) => { query.page = p; load() }" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑商品' : '新增商品'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" />
        </el-form-item>
        <el-form-item label="封面URL">
          <el-input v-model="form.coverUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="skuVisible" :title="`SKU 管理 · ${skuProduct?.name || ''}`" width="640px">
      <el-table :data="skus" border size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" />
        <el-table-column label="价格" width="110">
          <template #default="{ row }">{{ money(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="specJson" label="规格" width="160" />
        <el-table-column label="操作" width="130">
          <template #default="{ row }">
            <el-button link type="primary" @click="openSkuEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeSku(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-divider content-position="left">{{ skuEditing ? '编辑 SKU' : '新增 SKU' }}</el-divider>
      <el-form :model="skuForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="skuForm.title" placeholder="如：8GB+256GB 黑色" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="skuForm.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="规格JSON">
          <el-input v-model="skuForm.specJson" placeholder='{"color":"黑色"}' />
        </el-form-item>
        <el-form-item v-if="!skuEditing" label="初始库存">
          <el-input-number v-model="skuForm.initialStock" :min="0" :step="10" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveSku">{{ skuEditing ? '保存修改' : '新增 SKU' }}</el-button>
          <el-button v-if="skuEditing" @click="openSkuCreate">取消编辑</el-button>
          <el-button v-else @click="openSkuCreate">清空</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

