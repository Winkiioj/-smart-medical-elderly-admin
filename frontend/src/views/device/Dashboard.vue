<template>
  <div class="device-dashboard">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>设备管理</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">设备管理 Dashboard</h2>

    <!-- 数字卡片行 -->
    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/devices?status=1')">
          <div class="stat-value" style="color: #67C23A">{{ dashboard.stats?.online || 0 }}</div>
          <div class="stat-label">在线设备</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/devices?status=2')">
          <div class="stat-value" style="color: #F56C6C">{{ dashboard.stats?.offline || 0 }}</div>
          <div class="stat-label">离线设备</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" @click="$router.push('/devices?status=3')">
          <div class="stat-value" style="color: #E6A23C">{{ dashboard.stats?.repairing || 0 }}</div>
          <div class="stat-label">维修中</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value" style="color: #909399">{{ dashboard.stats?.scrapped || 0 }}</div>
          <div class="stat-label">已报废</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 维修中设备 + 质保到期 -->
    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: bold">维修中设备</span>
            <el-button size="small" style="float: right" @click="$router.push('/devices?status=3')">全部</el-button>
          </template>
          <el-table :data="dashboard.repairingDevices" stripe v-loading="loading" size="small">
            <el-table-column prop="deviceNo" label="编号" width="160" />
            <el-table-column prop="deviceName" label="名称" />
            <el-table-column prop="deviceType" label="类型" width="90" />
            <el-table-column label="更新" width="100">
              <template #default="{ row }">
                {{ row.updateTime?.substring(0, 10) }}
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!dashboard.repairingDevices?.length" description="暂无维修中设备" :image-size="60" />
          <div v-if="dashboard.repairingTotal > repairPageSize" style="margin-top:12px;display:flex;justify-content:flex-end">
            <el-pagination
              v-model:current-page="repairPageNo"
              :page-size="repairPageSize"
              :total="dashboard.repairingTotal"
              layout="prev, pager, next"
              size="small"
              @current-change="fetchData"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: bold">质保即将到期</span>
          </template>
          <el-table :data="dashboard.expiringDevices" stripe v-loading="loading" size="small">
            <el-table-column prop="deviceNo" label="编号" width="160" />
            <el-table-column prop="deviceName" label="名称" />
            <el-table-column label="保修截止" width="110">
              <template #default="{ row }">
                <el-tag :type="warrantyTag(row).type" size="small">{{ row.warrantyEnd }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!dashboard.expiringDevices?.length" description="暂无即将到期设备" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDeviceDashboard } from '@/api/device'

const loading = ref(false)
const repairPageNo = ref(1)
const repairPageSize = 8
const dashboard = reactive({ stats: {}, repairingDevices: [], repairingTotal: 0, expiringDevices: [] })

const warrantyTag = (row) => {
  if (!row.warrantyEnd) return { type: '', text: '' }
  const end = new Date(row.warrantyEnd)
  const now = new Date()
  const days = Math.floor((end - now) / (1000 * 60 * 60 * 24))
  if (days <= 0) return { type: 'danger', text: '已过期' }
  return { type: 'warning', text: `剩余${days}天` }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDeviceDashboard({ repairPageNo: repairPageNo.value, repairPageSize })
    if (res.code === 200) {
      Object.assign(dashboard, res.data)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
.stat-card {
  cursor: pointer;
  text-align: center;
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
</style>
