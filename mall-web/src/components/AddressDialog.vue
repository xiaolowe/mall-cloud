<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api/mall'
import { useUiStore } from '../stores/ui'

const ui = useUiStore()
const addresses = ref([])
const saving = ref(false)
const formRef = ref()

const form = reactive({
  receiverName: '',
  receiverPhone: '',
  detail: '',
  isDefault: true
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  receiverPhone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

async function load() {
  try {
    addresses.value = await userApi.addresses()
  } catch (e) {
    addresses.value = []
  }
}

watch(
  () => ui.addressOpen,
  (open) => {
    if (open) load()
  }
)

async function save() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await userApi.addAddress({ ...form })
    ElMessage.success('地址已保存')
    form.receiverName = ''
    form.receiverPhone = ''
    form.detail = ''
    await load()
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <el-dialog v-model="ui.addressOpen" title="收货地址" width="480px">
    <div v-for="address in addresses" :key="address.id" class="address-item">
      <div class="name">
        {{ address.receiverName }} {{ address.receiverPhone }}
        <el-tag v-if="address.isDefault" size="small" type="success">默认</el-tag>
      </div>
      <div class="detail">{{ address.detail }}</div>
    </div>

    <el-divider content-position="left">新增地址</el-divider>

    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="收货人" prop="receiverName">
        <el-input v-model="form.receiverName" placeholder="请输入收货人姓名" />
      </el-form-item>
      <el-form-item label="手机号" prop="receiverPhone">
        <el-input v-model="form.receiverPhone" maxlength="11" placeholder="11 位手机号" />
      </el-form-item>
      <el-form-item label="详细地址" prop="detail">
        <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="省市区 + 详细地址" />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="form.isDefault">设为默认地址</el-checkbox>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="ui.addressOpen = false">关闭</el-button>
      <el-button type="primary" :loading="saving" @click="save">保存地址</el-button>
    </template>
  </el-dialog>
</template>
