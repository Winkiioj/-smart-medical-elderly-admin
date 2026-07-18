<template>
  <el-container style="height: 100vh">
    <!-- 左侧菜单 -->
    <el-aside width="220px" style="background: #304156; overflow-y: auto">
      <div style="height: 60px; display: flex; align-items: center; justify-content: center">
        <span style="color: #fff; font-size: 16px; font-weight: bold">智慧医养管理系统</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/device-dashboard">
          <el-icon><Monitor /></el-icon>
          <span>Dashboard</span>
        </el-menu-item>
        <el-menu-item index="/devices">
          <el-icon><Setting /></el-icon>
          <span>设备台账管理</span>
        </el-menu-item>
    </el-menu>
  </el-aside>

  <!-- 右侧区域 -->
  <el-container>
    <!-- 顶部栏 -->
    <el-header style="background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: space-between">
      <span style="color: #303133; font-weight: bold">设备管理</span>
        <el-dropdown>
          <span style="display: flex; align-items: center; cursor: pointer; color: #303133">
            <el-avatar size="small" style="margin-right: 8px">管</el-avatar>
            设备管理员
            <el-icon style="margin-left: 4px"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>个人信息</el-dropdown-item>
              <el-dropdown-item>修改密码</el-dropdown-item>
              <el-dropdown-item divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <!-- 主内容区 -->
      <el-main style="background: #f0f2f5">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { Monitor, Setting, ArrowDown } from '@element-plus/icons-vue'
</script>
