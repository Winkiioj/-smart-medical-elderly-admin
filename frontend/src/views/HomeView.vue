<template>
  <div class="home-container">
    <div class="welcome-card">
      <img src="@/assets/logo.svg" alt="logo" class="logo" width="80" height="80" />
      <h1>智慧医养管理系统</h1>
      <p class="subtitle">Smart Medical-Elderly Care Management System</p>
      <el-divider />
      <p class="status">🎯 项目骨架已就绪，等待各业务模块开发</p>
      <el-space style="margin-top: 20px">
        <el-tag type="success">Vue 3 + Vite</el-tag>
        <el-tag type="warning">Element Plus</el-tag>
        <el-tag type="danger">Spring Boot 3.5.13</el-tag>
        <el-tag>MyBatis Plus 3.5.12</el-tag>
      </el-space>
    </div>
  </div>
</template>

<script setup>
// 首页占位 — 后续替换为登录页或 Dashboard
</script>

<style scoped>
.home-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.welcome-card {
  background: #fff;
  border-radius: 16px;
  padding: 60px 80px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0,0,0,0.15);
}
.logo { margin-bottom: 16px; }
h1 { font-size: 28px; color: #303133; margin: 0 0 8px; }
.subtitle { font-size: 14px; color: #909399; margin: 0; }
.status { color: #67c23a; font-size: 14px; margin: 0; }
</style>
