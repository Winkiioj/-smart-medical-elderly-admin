<template>
  <el-container class="layout">
    <!-- ===== 侧边栏 ===== -->
    <el-aside width="220px">
      <div class="logo-area">
        <span>🏥 智慧医养</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/doctor-dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>医生工作台</span>
        </el-menu-item>
        <el-menu-item index="/doctors">
          <el-icon><UserFilled /></el-icon>
          <span>医生管理</span>
        </el-menu-item>
        <el-menu-item index="/assign">
          <el-icon><Connection /></el-icon>
          <span>老人分配</span>
        </el-menu-item>
        <el-menu-item index="/reports">
          <el-icon><Document /></el-icon>
          <span>报表统计</span>
        </el-menu-item>

        <el-menu-item index="/elderly">
          <el-icon><Avatar /></el-icon>
          <span>老人档案</span>
        </el-menu-item>
        <el-menu-item index="/health-import">
          <el-icon><Upload /></el-icon>
          <span>健康导入</span>
        </el-menu-item>
        <el-menu-item index="/health-trend">
          <el-icon><TrendCharts /></el-icon>
          <span>趋势图</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- ===== 右侧：顶栏 + 内容 ===== -->
    <el-container>
      <!-- 顶栏 -->
      <el-header height="56px">
        <span class="header-title">智慧医养管理系统</span>
        <div class="header-right">
          <span class="user-info">👤 admin</span>
          <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataBoard, UserFilled, Connection, Document, Avatar, Upload, TrendCharts } from '@element-plus/icons-vue'
import { delStorage } from '@/utils/localStorage.js'

const router = useRouter()
const route = useRoute()

// 当前路由路径 → 侧边栏高亮
const activeMenu = computed(() => route.path)

const handleLogout = () => {
  delStorage('Token')
  ElMessage.success('已退出登录')
  router.push('/')
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

/* ===== 侧边栏 ===== */
.el-aside {
  background: #304156;
  overflow: hidden;
}
.logo-area {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  letter-spacing: 2px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.el-menu {
  border-right: none;
}

/* ===== 顶栏 ===== */
.el-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.header-title {
  font-size: 16px;
  color: #303133;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-info {
  font-size: 14px;
  color: #606266;
}

/* ===== 内容区 ===== */
.el-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>
