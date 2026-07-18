<template>
  <div class="dashboard">
    <h2 style="margin-bottom: 20px">{{ isComAdmin ? (data.community || '本社区') + ' — 工作台' : '全局工作台' }}</h2>

    <!-- ===== 机构管理员：全局四卡片 ===== -->
    <el-row v-if="isOrgAdmin" :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card">
          <div class="stat-icon" style="background:#e6f7ff"><el-icon size="28" color="#409eff"><OfficeBuilding /></el-icon></div>
          <div class="stat-text"><div class="stat-number">{{ data.communityCount }}</div><div class="stat-label">接入社区</div></div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card">
          <div class="stat-icon" style="background:#f6ffed"><el-icon size="28" color="#67c23a"><User /></el-icon></div>
          <div class="stat-text"><div class="stat-number">{{ data.elderlyTotal }}</div><div class="stat-label">在院老人总数</div></div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card">
          <div class="stat-icon" style="background:#fff7e6"><el-icon size="28" color="#e6a23c"><UserFilled /></el-icon></div>
          <div class="stat-text"><div class="stat-number">{{ data.doctorTotal }}</div><div class="stat-label">签约医生</div></div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card">
          <div class="stat-icon" style="background:#fef0f0"><el-icon size="28" color="#f56c6c"><Monitor /></el-icon></div>
          <div class="stat-text"><div class="stat-number">{{ data.deviceOnlineRate }}<span class="unit">%</span></div><div class="stat-label">设备在线率</div></div>
        </div></el-card>
      </el-col>
    </el-row>

    <!-- ===== 社区管理员：本社区四卡片 ===== -->
    <el-row v-if="isComAdmin" :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card">
          <div class="stat-icon" style="background:#e6f7ff"><el-icon size="28" color="#409eff"><User /></el-icon></div>
          <div class="stat-text"><div class="stat-number">{{ data.elderlyTotal }}</div><div class="stat-label">在院老人</div></div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card">
          <div class="stat-icon" style="background:#f6ffed"><el-icon size="28" color="#67c23a"><Plus /></el-icon></div>
          <div class="stat-text"><div class="stat-number">{{ data.newThisMonth }}</div><div class="stat-label">本月新增老人</div></div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card">
          <div class="stat-icon" style="background:#fff7e6"><el-icon size="28" color="#e6a23c"><WarningFilled /></el-icon></div>
          <div class="stat-text"><div class="stat-number">{{ data.pendingWarnings }}</div><div class="stat-label">待处理预警</div></div>
        </div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><div class="stat-card">
          <div class="stat-icon" style="background:#fef0f0"><el-icon size="28" color="#f56c6c"><Monitor /></el-icon></div>
          <div class="stat-text"><div class="stat-number">{{ data.deviceOnlineRate }}<span class="unit">%</span></div><div class="stat-label">设备在线率</div></div>
        </div></el-card>
      </el-col>
    </el-row>

    <!-- ===== 机构管理员：各社区对标表 + 快捷入口 ===== -->
    <el-row v-if="isOrgAdmin" :gutter="20" style="margin-top:20px">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header><span style="font-weight:bold">各社区概况</span></template>
          <el-table :data="data.communityOverview" stripe size="small" v-loading="loading">
            <el-table-column prop="community" label="社区" />
            <el-table-column prop="elderly" label="在院老人" align="center" />
            <el-table-column prop="doctors" label="签约医生" align="center" />
            <el-table-column label="待处理预警" align="center">
              <template #default="{ row }">
                <el-tag :type="row.warnings > 0 ? 'danger' : 'success'" size="small">{{ row.warnings }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="100">
              <template #default="{ row }">
                <el-button size="small" type="primary" link @click="$router.push('/reports'); setTimeout(() => viewCommunityFromDash(row.community), 300)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!data.communityOverview?.length" description="暂无社区数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span style="font-weight:bold">快捷入口</span></template>
          <div class="shortcut" @click="$router.push('/communities')">
            <el-icon size="24" color="#409eff"><OfficeBuilding /></el-icon><span>社区管理</span>
          </div>
          <div class="shortcut" @click="$router.push('/reports')">
            <el-icon size="24" color="#e6a23c"><Document /></el-icon><span>报表统计</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ===== 社区管理员：快捷入口 ===== -->
    <el-row v-if="isComAdmin" :gutter="20" style="margin-top:20px">
      <el-col :span="8">
        <div class="shortcut-card" @click="$router.push('/doctors')">
          <el-icon size="24" color="#409eff"><UserFilled /></el-icon><span>医生管理</span>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="shortcut-card" @click="$router.push('/assign')">
          <el-icon size="24" color="#67c23a"><Connection /></el-icon><span>老人分配</span>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="shortcut-card" @click="$router.push('/reports')">
          <el-icon size="24" color="#e6a23c"><Document /></el-icon><span>本社区报表</span>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onMounted } from 'vue'
import { User, Plus, WarningFilled, Monitor, UserFilled, Connection, Document, OfficeBuilding } from '@element-plus/icons-vue'
import { getStorage } from '@/utils/localStorage.js'
import { getDashboard } from '@/api/reports.js'

const roleCode = computed(() => getStorage('RoleCode') || '')
const isOrgAdmin = computed(() => roleCode.value === 'ORG_ADMIN')
const isComAdmin = computed(() => roleCode.value === 'COM_ADMIN')

const loading = ref(false)
const data = reactive({ communityOverview: [] })

const viewCommunityFromDash = (com) => {
  // 通过 localStorage 传递社区名给 ReportsView
  localStorage.setItem('_viewCommunity', com)
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getDashboard()
    if (res.code === 200) Object.assign(data, res.data)
  } finally { loading.value = false }
})
</script>

<style scoped>
.stat-card { display:flex; align-items:center; gap:16px; }
.stat-icon { width:56px; height:56px; border-radius:12px; display:flex; align-items:center; justify-content:center; }
.stat-number { font-size:28px; font-weight:bold; color:#303133; }
.stat-number .unit { font-size:16px; font-weight:normal; color:#909399; }
.stat-label { font-size:13px; color:#909399; margin-top:4px; }
.shortcut { display:flex; flex-direction:column; align-items:center; gap:8px; padding:20px; border-radius:8px; cursor:pointer; transition:background .2s; }
.shortcut:hover { background:#f5f7fa; }
.shortcut span { font-size:14px; color:#606266; }
.shortcut-card { display:flex; flex-direction:column; align-items:center; gap:8px; padding:24px; border-radius:8px; cursor:pointer; background:#fff; box-shadow:0 1px 4px rgba(0,0,0,0.06); transition:transform .2s; }
.shortcut-card:hover { transform:translateY(-2px); box-shadow:0 2px 8px rgba(0,0,0,0.1); }
.shortcut-card span { font-size:14px; color:#606266; font-weight:500; }
</style>
