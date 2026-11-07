<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { useCartStore } from '../stores/cart'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const cart = useCartStore()

const tab = ref('login')
const submitting = ref(false)
const loginRef = ref()
const registerRef = ref()

const loginForm = reactive({ username: 'demo', password: '123456' })
const registerForm = reactive({ username: '', password: '', nickname: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名长度为 3-32 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' }
  ]
}

async function afterAuth() {
  await cart.load()
  router.push(route.query.redirect || '/')
}

async function submitLogin() {
  const valid = await loginRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await auth.login(loginForm)
    ElMessage.success('登录成功')
    await afterAuth()
  } finally {
    submitting.value = false
  }
}

async function submitRegister() {
  const valid = await registerRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await auth.register(registerForm)
    ElMessage.success('注册成功，已自动登录')
    await afterAuth()
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1 class="login-title">欢迎来到 Mall Cloud</h1>
      <p class="login-desc">登录后即可加入购物车、下单和模拟支付</p>

      <el-tabs v-model="tab">
        <el-tab-pane label="登录" name="login">
          <el-form
            ref="loginRef"
            :model="loginForm"
            :rules="loginRules"
            label-position="top"
            @keyup.enter="submitLogin"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-button type="primary" style="width: 100%" :loading="submitting" @click="submitLogin">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form
            ref="registerRef"
            :model="registerForm"
            :rules="registerRules"
            label-position="top"
            @keyup.enter="submitRegister"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="3-32 位" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="至少 6 位" />
            </el-form-item>
            <el-form-item label="昵称（可选）">
              <el-input v-model="registerForm.nickname" placeholder="展示用昵称" />
            </el-form-item>
            <el-button type="primary" style="width: 100%" :loading="submitting" @click="submitRegister">
              注册并登录
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <p class="login-tip">演示支付为本地模拟，不会产生真实扣款。</p>
    </div>
  </div>
</template>
