<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { categoryApi } from '../api/admin'

const list = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const form = reactive({ name: '', sort: 0 })

async function load() {
  loading.value = true
  try {
    list.value = await categoryApi.list()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  form.name = ''
  form.sort = 0
  dialogVisible.value = true
}

function openEdit(row) {
  editing.value = row
  form.name = row.name
  form.sort = row.sort
  dialogVisible.value = true
}

async function save() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  if (editing.value) {
    await categoryApi.update(editing.value.id, { name: form.name, sort: form.sort })
  } else {
    await categoryApi.create({ name: form.name, sort: form.sort })
  }
  ElMessage.success('已保存')
  dialogVisible.value = false
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
  await categoryApi.remove(row.id)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<template>
  <div class="page-card">
    <div class="toolbar">
      <el-button type="primary" @click="openCreate">新增分类</el-button>
      <span class="spacer"></span>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑分类' : '新增分类'" width="400px">
      <el-form label-width="70px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
