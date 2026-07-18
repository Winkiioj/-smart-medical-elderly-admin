<template>
  <div class="login-page">
    <!-- ========== 左侧：品牌宣传区 ========== -->
    <div class="login-left">
      <div class="left-content">
        <h1>智慧医养管理系统</h1>
        <p>Smart Medical-Elderly Care Management System</p>
        <div class="feature-list">
          <div class="feature-item">🏥 社区健康数据一站式管理</div>
          <div class="feature-item">📊 实时预警与智能监测</div>
          <div class="feature-item">👨‍⚕️ 医生-老人签约服务体系</div>
          <div class="feature-item">📋 健康评估与随访追踪</div>
        </div>
      </div>
    </div>

    <!-- ========== 右侧：登录表单区 ========== -->
    <div class="login-right">
      <div class="login-form">
        <h2>欢迎登录</h2>
        <p class="form-subtitle">请输入您的账号信息</p>

        <el-input
          v-model="username"
          placeholder="用户名"
          size="large"
          :prefix-icon="User"
        />
        <el-input
          v-model="password"
          placeholder="密码"
          type="password"
          size="large"
          show-password
          :prefix-icon="Lock"
          @keyup.enter="handleLogin"
        />

        <el-button
          type="primary"
          size="large"
          :loading="loading"
          style="width: 100%"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request.js'
import { setStorage } from '@/utils/localStorage.js'

const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)

// 角色 → 首页路由映射
const ROLE_HOME = {
  ORG_ADMIN: '/dashboard',
  COM_ADMIN: '/dashboard',
  DOCTOR: '/doctor-dashboard',
  DEVICE_ADMIN: '/device-dashboard',
}

const handleLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await request({
      url: '/api/auth/login',
      method: 'post',
      data: { username: username.value, password: password.value },
    })
    const data = res.data
    setStorage('Token', data.token)
    setStorage('RoleCode', data.roleCode || '')
    setStorage('RoleName', data.roleName || '')
    setStorage('RealName', data.realName || '')
    setStorage('Community', data.community || '')
    ElMessage.success(`登录成功！欢迎 ${data.roleName || ''}`)
    const home = ROLE_HOME[data.roleCode] || '/dashboard'
    router.push(home)
  } catch (err) {
    // request.js 已处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}
.left-content {
  color: #fff;
  padding: 60px;
  max-width: 480px;
}
.left-content h1 {
  font-size: 32px;
  margin-bottom: 12px;
}
.left-content p {
  font-size: 14px;
  opacity: 0.85;
  margin-bottom: 48px;
}
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.feature-item {
  font-size: 16px;
  opacity: 0.92;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}
.login-form {
  width: 380px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.login-form h2 {
  font-size: 26px;
  color: #303133;
  margin: 0;
}
.form-subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}
</style>
