<template>
  <div class="login-page">
    <div class="top-logo">
      <svg class="top-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="2" y1="12" x2="22" y2="12"></line>
        <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"></path>
      </svg>
      全球新闻管理系统
    </div>
    <div class="login-container">
      <div class="brand">
        <h1>整合全球信息，使人人受益</h1>
        <p class="slogan">真实 · 客观 · 时效 · 公共价值</p>
      </div>
      <div class="login-box">
        <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
          <h2>登录至你的账户</h2>
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" placeholder="请输入密码" type="password"
                      size="large" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="login-btn"
                     style="width:100%" @click="handleLogin">
            登录
          </el-button>
          <div class="links">
            <a href="#" @click.prevent>忘记密码?</a>
            <router-link to="/register">注册</router-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = ref({ username: '', password: '' })
const rules = {}

async function handleLogin() {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(form.value)
        userStore.setUser(res.data)
        ElMessage.success('登录成功')
        router.push(res.data.role === 1 ? '/admin' : '/')
      } catch (err) {
        // 请求失败由拦截器处理
      } finally {
        loading.value = false
      }
    } else {
      ElMessage.warning('请检查输入格式')
      return false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #000000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}
.login-container {
  display: flex;
  gap: 120px;
  align-items: center;
  justify-content: center;
  max-width: 1200px;
  width: 100%;
  padding: 0 40px;
}

@media (max-width: 900px) {
  .login-container {
    flex-direction: column;
    gap: 40px;
  }
}

.brand {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.top-logo {
  position: absolute;
  top: 50px;
  left: 60px;
  color: #ffffff;
  font-size: 48px;
  font-weight: bold;
  letter-spacing: 2px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.top-icon {
  width: 48px;
  height: 48px;
  color: #ff2a55;
}
.brand h1 {
  font-size: 48px;
  font-weight: bold;
  color: #ffffff;
  margin: 0;
  letter-spacing: 2px;
  line-height: 1.3;
}
.slogan {
  color: #ff2a55;
  font-size: 32px;
  font-weight: bold;
  margin: 20px 0 60px;
  letter-spacing: 2px;
}
.login-box {
  flex: 0 0 auto;
}
.login-form {
  background: #141414;
  padding: 40px;
  border-radius: 8px;
  width: 400px;
  box-sizing: border-box;
}
.login-form h2 {
  margin: 0 0 30px;
  font-size: 20px;
  color: #ffffff;
  font-weight: bold;
}
:deep(.el-input__wrapper) {
  background-color: #212121;
  box-shadow: none !important;
  border-radius: 4px;
  padding: 8px 12px;
}
:deep(.el-input__inner) {
  color: #ffffff !important;
  height: 36px;
  font-size: 14px;
}
:deep(.el-input__inner::placeholder) {
  color: #666;
}
:deep(.el-input__suffix .el-icon) {
  color: #888;
}
.login-btn {
  background-color: #ff2a55;
  border-color: #ff2a55;
  color: #ffffff;
  font-size: 16px;
  font-weight: bold;
  height: 50px;
  border-radius: 4px;
  margin-top: 10px;
  transition: all 0.3s;
}
.login-btn:hover, .login-btn:focus {
  background-color: #e6254c;
  border-color: #e6254c;
}
.links {
  margin-top: 20px;
  font-size: 14px;
  display: flex;
  justify-content: space-between;
}
.links a { 
  color: #ff2a55; 
  text-decoration: none; 
}
.links a:hover {
  text-decoration: underline;
}
</style>