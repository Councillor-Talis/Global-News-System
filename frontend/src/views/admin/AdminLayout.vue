<template>
  <div class="admin-layout">
    <div class="sidebar">
      <div class="logo">GNS 管理后台</div>
      <el-menu router default-active="/admin/dashboard"
               background-color="#1e293b" text-color="#94a3b8" active-text-color="#fff">
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>数据概览
        </el-menu-item>
        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon>文章管理
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>用户管理
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <span>{{ userStore.userInfo?.username }}</span>
        <el-button text style="color:#94a3b8" @click="handleLogout">退出</el-button>
      </div>
    </div>
    <div class="main">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import { logout } from '../../api/user'

const userStore = useUserStore()
const router = useRouter()

async function handleLogout() {
  try { await logout() } catch {}
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout { display: flex; min-height: 100vh; }
.sidebar { width: 220px; background: #1e293b; display: flex; flex-direction: column; flex-shrink: 0; }
.logo { color: #fff; font-size: 16px; font-weight: 700; padding: 20px 20px 8px; }
.main { flex: 1; background: #f8fafc; overflow: auto; }
.sidebar-footer {
  margin-top: auto; padding: 16px 20px;
  border-top: 1px solid #334155;
  display: flex; justify-content: space-between; align-items: center;
  font-size: 13px; color: #94a3b8;
}
</style>