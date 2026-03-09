<template>
  <div class="user-dashboard">
    <el-container>
      <el-header>
        <div class="header-content">
          <h2>设备租赁系统</h2>
          <div class="user-info">
            <span>{{ userStore.username }}</span>
            <el-tag :type="getRoleType(userStore.role)">{{ getRoleText(userStore.role) }}</el-tag>
            <el-button @click="logout" type="danger" size="small">退出</el-button>
          </div>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-menu
            :default-active="activeMenu"
            class="el-menu-vertical"
            @select="handleMenuSelect"
          >
            <el-menu-item index="equipment">
              <el-icon><List /></el-icon>
              <span>设备列表</span>
            </el-menu-item>
            <el-menu-item index="orders">
              <el-icon><Document /></el-icon>
              <span>我的订单</span>
            </el-menu-item>
            <el-menu-item index="delivery">
              <el-icon><Van /></el-icon>
              <span>配送管理</span>
            </el-menu-item>
            <el-menu-item index="profile">
              <el-icon><User /></el-icon>
              <span>个人信息</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.role === 'device_admin' || userStore.role === 'system_admin'" index="equipment-manage">
              <el-icon><Setting /></el-icon>
              <span>设备管理</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.role === 'device_admin' || userStore.role === 'system_admin'" index="order-manage">
              <el-icon><Management /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.role === 'device_admin' || userStore.role === 'system_admin'" index="deduction-manage">
              <el-icon><Money /></el-icon>
              <span>押金扣除</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.role === 'system_admin'" index="user-manage">
              <el-icon><UserFilled /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item v-if="userStore.role === 'system_admin'" index="system-manage">
              <el-icon><Tools /></el-icon>
              <span>系统管理</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { List, Document, User, Setting, Management, UserFilled, Tools, Van, Money } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const activeMenu = ref('equipment')

const handleMenuSelect = (index) => {
  activeMenu.value = index
  router.push(`/dashboard/${index}`)
}

const logout = () => {
  userStore.logout()
  ElMessage.success('退出成功')
  router.push('/')
}

const getRoleType = (role) => {
  const types = {
    'user': '',
    'device_admin': 'warning',
    'system_admin': 'danger'
  }
  return types[role] || ''
}

const getRoleText = (role) => {
  const texts = {
    'user': '普通用户',
    'device_admin': '设备管理员',
    'system_admin': '系统管理员'
  }
  return texts[role] || '未知'
}
</script>

<style scoped>
.user-dashboard {
  height: 100vh;
}

.el-header {
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-content h2 {
  margin: 0;
  font-size: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.el-aside {
  background-color: #f5f5f5;
  border-right: 1px solid #e6e6e6;
}

.el-main {
  padding: 20px;
  background-color: #fff;
}

.el-menu-vertical {
  border-right: none;
}
</style>
