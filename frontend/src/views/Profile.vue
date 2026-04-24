<template>
  <div class="page">
    <NavBar :categories="[]" />
    <div class="container">

      <div class="back" @click="$router.back()">← 返回</div>
      <h2>个人中心</h2>

      <div class="profile-card" v-loading="loading">

        <!-- 头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrap" @click="triggerFileInput">
            <img v-if="avatarUrl" :src="avatarUrl" class="avatar-img" />
            <el-avatar v-else :size="100" icon="UserFilled" />
            <div class="avatar-mask">
              <el-icon size="24"><Camera /></el-icon>
              <span>更换头像</span>
            </div>
          </div>
          <input
              ref="fileInput"
              type="file"
              accept=".jpg,.jpeg,.png,.gif,.webp"
              style="display:none"
              @change="handleAvatarChange" />
          <div class="avatar-tip">点击头像更换，支持 JPG/PNG/WEBP，最大 2MB</div>
        </div>

        <el-divider />

        <!-- 基本信息 -->
        <div class="info-section">
          <div class="info-row">
            <span class="info-label">用户ID</span>
            <span class="info-value muted">{{ userInfo.id }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ userInfo.email || '未设置' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">角色</span>
            <el-tag :type="userInfo.role === 1 ? 'warning' : 'info'" size="small">
              {{ userInfo.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </div>
          <div class="info-row">
            <span class="info-label">注册时间</span>
            <span class="info-value muted">{{ userInfo.createdAt }}</span>
          </div>
        </div>

        <el-divider />

        <!-- 修改用户名 -->
        <div class="edit-section">
          <h3>修改用户名</h3>
          <div class="edit-row">
            <el-input
                v-model="newUsername"
                placeholder="输入新用户名（3-20位）"
                maxlength="20"
                show-word-limit
                style="max-width: 300px" />
            <el-button
                type="primary"
                :loading="usernameLoading"
                :disabled="!newUsername.trim() || newUsername === userInfo.username"
                @click="handleUpdateUsername">
              保存修改
            </el-button>
          </div>
          <div class="edit-tip">当前用户名：{{ userInfo.username }}</div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import NavBar from '../components/NavBar.vue'
import { getProfile, updateUsername, uploadAvatar } from '../api/user'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
const loading = ref(false)
const userInfo = ref({})
const avatarUrl = ref('')
const newUsername = ref('')
const usernameLoading = ref(false)
const fileInput = ref(null)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getProfile()
    userInfo.value = res.data
    avatarUrl.value = res.data.avatar || ''
    newUsername.value = res.data.username
  } finally {
    loading.value = false
  }
})

function triggerFileInput() {
  fileInput.value.click()
}

async function handleAvatarChange(e) {
  const file = e.target.files[0]
  if (!file) return

  // 本地预览
  const reader = new FileReader()
  reader.onload = (ev) => { avatarUrl.value = ev.target.result }
  reader.readAsDataURL(file)

  // 上传
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await uploadAvatar(formData)
    avatarUrl.value = res.data

    // 同步更新 Pinia 里的用户信息
    userStore.userInfo.avatar = res.data
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))

    ElMessage.success('头像更新成功')
  } catch (e) {
    // 上传失败恢复原头像
    avatarUrl.value = userInfo.value.avatar || ''
  }

  // 清空 input，允许重复选同一文件
  e.target.value = ''
}

async function handleUpdateUsername() {
  if (newUsername.value.length < 3) {
    ElMessage.warning('用户名至少3位')
    return
  }
  usernameLoading.value = true
  try {
    await updateUsername({ username: newUsername.value })
    ElMessage.success('用户名修改成功')

    // 同步更新本地存储
    userInfo.value.username = newUsername.value
    userStore.userInfo.username = newUsername.value
    localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
  } finally {
    usernameLoading.value = false
  }
}
</script>

<style scoped>
.page { min-height: 100vh; background: #f8fafc; }
.container { max-width: 680px; margin: 0 auto; padding: 32px 24px; }
.back { cursor: pointer; color: #3b82f6; margin-bottom: 16px; font-size: 14px; }
h2 { margin: 0 0 24px; color: #1e293b; font-size: 22px; }
h3 { margin: 0 0 16px; color: #1e293b; font-size: 16px; font-weight: 600; }

.profile-card {
  background: #fff; border-radius: 16px;
  border: 1px solid #e2e8f0; padding: 32px;
}

/* 头像 */
.avatar-section { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.avatar-wrap {
  position: relative; cursor: pointer;
  border-radius: 50%; overflow: hidden;
  width: 100px; height: 100px;
}
.avatar-img {
  width: 100px; height: 100px;
  object-fit: cover; border-radius: 50%;
  border: 3px solid #e2e8f0;
}
.avatar-mask {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 4px; opacity: 0; transition: opacity .2s;
  color: #fff; font-size: 12px;
  border-radius: 50%;
}
.avatar-wrap:hover .avatar-mask { opacity: 1; }
.avatar-tip { font-size: 12px; color: #94a3b8; }

/* 基本信息 */
.info-section { display: flex; flex-direction: column; gap: 16px; }
.info-row { display: flex; align-items: center; gap: 16px; }
.info-label { font-size: 14px; color: #64748b; width: 80px; flex-shrink: 0; }
.info-value { font-size: 14px; color: #1e293b; }
.info-value.muted { color: #94a3b8; }

/* 修改用户名 */
.edit-section {}
.edit-row { display: flex; gap: 12px; align-items: center; margin-bottom: 8px; }
.edit-tip { font-size: 12px; color: #94a3b8; }
</style>