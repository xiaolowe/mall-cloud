<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  DataLine, Goods, Files, Box, List, User
} from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
const title = computed(() => route.meta.title || '管理后台')
</script>

<template>
  <el-container class="admin-layout">
    <el-aside width="210px" class="admin-aside">
      <div class="admin-logo">Mall<span>Cloud</span> 后台</div>
      <el-menu :default-active="activeMenu" router class="admin-menu" background-color="#1f2733"
               text-color="#c3cbd8" active-text-color="#ff8a5f">
        <el-menu-item index="/dashboard"><el-icon><DataLine /></el-icon><span>数据看板</span></el-menu-item>
        <el-menu-item index="/products"><el-icon><Goods /></el-icon><span>商品管理</span></el-menu-item>
        <el-menu-item index="/categories"><el-icon><Files /></el-icon><span>分类管理</span></el-menu-item>
        <el-menu-item index="/inventory"><el-icon><Box /></el-icon><span>库存管理</span></el-menu-item>
        <el-menu-item index="/orders"><el-icon><List /></el-icon><span>订单管理</span></el-menu-item>
        <el-menu-item index="/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header height="60px" class="admin-header">
        <span class="title">{{ title }}</span>
        <span class="spacer"></span>
        <el-tag type="warning" effect="plain" size="small">演示环境 · 未启用鉴权</el-tag>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
