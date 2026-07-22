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
        <!-- ===== 机构管理员 + 社区管理员 ===== -->
        <template v-if="isAdmin || isComAdmin">
          <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>工作台</span>
          </el-menu-item>
        </template>

        <!-- 社区管理员专属 -->
        <template v-if="isComAdmin">
          <el-menu-item index="/doctors">
            <el-icon><UserFilled /></el-icon>
            <span>医生管理</span>
          </el-menu-item>
          <el-menu-item index="/assign">
            <el-icon><Connection /></el-icon>
            <span>老人分配</span>
          </el-menu-item>
        </template>

        <!-- 机构管理员专属 -->
        <template v-if="isAdmin">
          <el-menu-item index="/communities">
            <el-icon><OfficeBuilding /></el-icon>
            <span>社区管理</span>
          </el-menu-item>
        </template>

        <!-- 管理员报表 -->
        <template v-if="isAdmin || isComAdmin">
          <el-menu-item index="/reports">
            <el-icon><Document /></el-icon>
            <span>报表统计</span>
          </el-menu-item>
          <el-menu-item index="/elderly">
            <el-icon><User /></el-icon>
            <span>老人档案</span>
          </el-menu-item>
          <el-menu-item index="/warnings">
            <el-icon><Warning /></el-icon>
            <span>预警记录</span>
          </el-menu-item>
          <el-menu-item index="/devices">
            <el-icon><Setting /></el-icon>
            <span>设备台账</span>
          </el-menu-item>
        </template>

        <!-- 机构管理员专属：医生档案 -->
        <template v-if="isAdmin">
          <el-menu-item index="/doctor-archive">
            <el-icon><UserFilled /></el-icon>
            <span>医生档案</span>
          </el-menu-item>
        </template>

        <!-- ===== 社区医生 ===== -->
        <template v-if="isDoctor">
          <el-menu-item index="/doctor-dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/elderly">
            <el-icon><User /></el-icon>
            <span>老人档案</span>
          </el-menu-item>
          <el-menu-item index="/elderly-new">
            <el-icon><Plus /></el-icon>
            <span>本月新增</span>
          </el-menu-item>
          <el-menu-item index="/health-import">
            <el-icon><Upload /></el-icon>
            <span>健康导入</span>
          </el-menu-item>
          <el-menu-item index="/health-trend">
            <el-icon><TrendCharts /></el-icon>
            <span>趋势图</span>
          </el-menu-item>
          <el-menu-item index="/device-repair">
            <el-icon><Tools /></el-icon>
            <span>设备报修</span>
          </el-menu-item>
          <el-menu-item index="/warnings">
            <el-icon><Warning /></el-icon>
            <span>预警记录</span>
          </el-menu-item>
          <el-menu-item index="/followup">
            <el-icon><Calendar /></el-icon>
            <span>随访管理</span>
          </el-menu-item>
          <el-menu-item index="/assessment">
            <el-icon><DocumentChecked /></el-icon>
            <span>评估报告</span>
          </el-menu-item>
          <el-menu-item index="/tags">
            <el-icon><Collection /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
        </template>

        <!-- ===== 设备管理员 ===== -->
        <template v-if="isDeviceAdmin">
          <el-menu-item index="/device-dashboard">
            <el-icon><Monitor /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/devices">
            <el-icon><Setting /></el-icon>
            <span>设备台账</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- ===== 右侧：顶栏 + 内容 ===== -->
    <el-container>
      <!-- 顶栏 -->
      <el-header height="56px">
        <span class="header-title">智慧医养管理系统</span>
        <div class="header-right">
          <!-- 消息通知 -->
          <el-popover placement="bottom" :width="360" trigger="click" @show="loadNotifications">
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
                <el-icon size="20" style="cursor: pointer"><Bell /></el-icon>
              </el-badge>
            </template>
            <div v-loading="notifyLoading" style="max-height: 320px; overflow-y: auto">
              <div v-if="notifications.length === 0" style="text-align: center; color: #909399; padding: 20px">
                暂无消息
              </div>
              <div
                v-for="n in notifications" :key="n.id"
                :style="{ padding: '10px 0', borderBottom: '1px solid #f0f0f0', cursor: 'pointer', background: n.isRead ? '#fff' : '#f0f9ff' }"
                @click="readNotification(n)"
              >
                <div style="font-size: 14px; font-weight: 500">{{ n.title }}</div>
                <div style="font-size: 12px; color: #909399; margin-top: 4px">{{ n.content }}</div>
                <div style="font-size: 11px; color: #c0c4cc; margin-top: 4px">{{ n.createTime }}</div>
              </div>
            </div>
            <div style="text-align: right; padding-top: 8px; border-top: 1px solid #f0f0f0; margin-top: 4px">
              <el-button size="small" text @click="handleMarkAllRead">全部已读</el-button>
            </div>
          </el-popover>

          <span class="user-info">👤 {{ realName }}（{{ roleName }}）</span>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  DataBoard, UserFilled, Connection, Document,
  User, Plus, Upload, TrendCharts, Tools,
  Monitor, Setting, Collection, Bell, OfficeBuilding, Warning, Calendar, DocumentChecked,
} from '@element-plus/icons-vue'
import { getStorage, delStorage } from '@/utils/localStorage.js'
import { getNotifications, getUnreadCount, markRead, markAllRead } from '@/api/notification.js'

const router = useRouter()
const route = useRoute()

const roleCode = computed(() => getStorage('RoleCode') || '')
const realName = computed(() => getStorage('RealName') || '')
const roleName = computed(() => getStorage('RoleName') || '')

// 通知
const unreadCount = ref(0)
const notifications = ref([])
const notifyLoading = ref(false)

const fetchUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) unreadCount.value = res.data
  } catch { /* ignore */ }
}

const loadNotifications = async () => {
  notifyLoading.value = true
  try {
    const res = await getNotifications(1, 10)
    if (res.code === 200) notifications.value = res.data || []
  } finally { notifyLoading.value = false }
}

const readNotification = async (n) => {
  if (n.isRead) return
  try {
    await markRead(n.id)
    n.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  } catch { /* ignore */ }
}

const handleMarkAllRead = async () => {
  try {
    await markAllRead()
    notifications.value.forEach(n => n.isRead = 1)
    unreadCount.value = 0
  } catch { /* ignore */ }
}

onMounted(() => fetchUnreadCount())

const isAdmin = computed(() => roleCode.value === 'ORG_ADMIN')
const isComAdmin = computed(() => roleCode.value === 'COM_ADMIN')
const isDoctor = computed(() => roleCode.value === 'DOCTOR')
const isDeviceAdmin = computed(() => roleCode.value === 'DEVICE_ADMIN')

const activeMenu = computed(() => route.path)

const handleLogout = () => {
  delStorage('Token')
  delStorage('RoleCode')
  delStorage('RoleName')
  delStorage('RealName')
  delStorage('Community')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.el-aside { background: #304156; overflow: hidden; }
.logo-area {
  height: 56px; display: flex; align-items: center; justify-content: center;
  color: #fff; font-size: 18px; font-weight: bold; letter-spacing: 2px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.el-menu { border-right: none; }
.el-header {
  background: #fff; display: flex; align-items: center; justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.header-title { font-size: 16px; color: #303133; }
.header-right { display: flex; align-items: center; gap: 16px; }
.user-info { font-size: 14px; color: #606266; }
.el-main { background: #f5f7fa; padding: 20px; }
.el-main .el-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border: 1px solid #e4e7ed;
}
</style>
