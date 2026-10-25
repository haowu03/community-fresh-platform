<template>
  <el-container class="layout-container">
    <!-- 左侧菜单 -->
    <el-aside width="220px" class="layout-aside">
      <div class="logo">生鲜配送管理端</div>
      <el-menu
        :default-active="route.path"
        router
        background-color="#001529"
        text-color="#a6adb4"
        active-text-color="#ffffff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-sub-menu index="product-center">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>商品中心</span>
          </template>
          <el-menu-item index="/category">分类管理</el-menu-item>
          <el-menu-item index="/product">商品管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="order-center">
          <template #title>
            <el-icon><Tickets /></el-icon>
            <span>订单中心</span>
          </template>
          <el-menu-item index="/order">订单管理</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="community">
          <template #title>
            <el-icon><ChatDotRound /></el-icon>
            <span>社区与团购</span>
          </template>
          <el-menu-item index="/pickup">自提点管理</el-menu-item>
          <el-menu-item index="/presale">预售管理</el-menu-item>
          <el-menu-item index="/groupbuy">团购管理</el-menu-item>
          <el-menu-item index="/post">帖子审核</el-menu-item>
        </el-sub-menu>
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统</span>
          </template>
          <el-menu-item index="/employee">员工管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏：当前页标题 + 用户名/退出 -->
      <el-header class="layout-header">
        <span class="page-title">{{ route.meta.title }}</span>
        <el-dropdown @command="onCommand">
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ userStore.name || '管理员' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 顶栏下拉命令
async function onCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100%;
}

.layout-aside {
  background-color: #001529;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-size: 17px;
  font-weight: bold;
  background-color: #002140;
}

.layout-aside :deep(.el-menu) {
  border-right: none;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.page-title {
  font-size: 16px;
  font-weight: 600;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  font-size: 14px;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 0;
}
</style>
