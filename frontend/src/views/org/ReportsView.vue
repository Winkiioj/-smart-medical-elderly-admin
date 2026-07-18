<template>
  <div class="reports-page">
    <!-- 面包屑 -->
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>报表统计</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">{{ isComAdmin ? community + ' — 社区报表' : '报表统计' }}</h2>

    <!-- Tab 切换 -->
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- ===== Tab 1：全局总览（仅机构管理员可见） ===== -->
      <el-tab-pane v-if="isOrgAdmin" label="全局总览" name="overview">
        <div v-loading="overviewLoading">
          <!-- 统计卡片 -->
          <el-row :gutter="16" style="margin-bottom: 20px">
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value" style="color: #409EFF">{{ overview.totals?.elderly || 0 }}</div>
                <div class="stat-label">在院老人总数</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value" style="color: #67C23A">{{ overview.totals?.doctors || 0 }}</div>
                <div class="stat-label">签约医生总数</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value" style="color: #E6A23C">{{ overview.totals?.devices || 0 }}</div>
                <div class="stat-label">设备总数</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value" style="color: #F56C6C">{{ overview.totals?.warnings || 0 }}</div>
                <div class="stat-label">待处理预警</div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 社区对比柱状图 -->
          <el-row :gutter="16" style="margin-bottom: 20px">
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header><span style="font-weight: bold">各社区老人分布</span></template>
                <div ref="elderlyChartRef" style="height: 320px"></div>
                <el-empty v-if="!overview.elderlyByCommunity?.length" description="暂无数据" :image-size="60" />
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header><span style="font-weight: bold">各社区设备状态</span></template>
                <div ref="deviceChartRef" style="height: 320px"></div>
                <el-empty v-if="!overview.devicesByCommunity?.length" description="暂无数据" :image-size="60" />
              </el-card>
            </el-col>
          </el-row>

          <!-- 社区对比表格 -->
          <el-card shadow="hover">
            <template #header>
              <span style="font-weight: bold">社区对比明细</span>
              <el-button size="small" style="float: right" @click="exportData('overview')">导出</el-button>
            </template>
            <el-table :data="communityTable" stripe border>
              <el-table-column prop="community" label="社区" width="140" />
              <el-table-column prop="elderly" label="在院老人" width="90" align="center" />
              <el-table-column prop="doctors" label="签约医生" width="90" align="center" />
              <el-table-column prop="devices" label="设备总数" width="90" align="center" />
              <el-table-column prop="online" label="在线设备" width="90" align="center" />
              <el-table-column prop="warnings" label="待处理预警" width="110" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.warnings > 0 ? 'danger' : 'success'" size="small">{{ row.warnings }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" align="center">
                <template #default="{ row }">
                  <el-button size="small" type="primary" link @click="viewCommunity(row.community)">
                    查看详情
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- ===== Tab 2：社区详情 ===== -->
      <el-tab-pane label="社区详情" name="community">
        <!-- 社区选择器：机构管理员可切换，社区管理员仅显示 -->
        <el-card shadow="hover" style="margin-bottom: 16px">
          <el-form :inline="true">
            <el-form-item v-if="isOrgAdmin" label="选择社区">
              <el-select v-model="selectedCommunity" placeholder="请选择社区" @change="loadCommunityDetail">
                <el-option v-for="c in overview.communities" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
            <el-form-item v-else>
              <span style="font-size:15px;font-weight:bold;color:#409eff">{{ selectedCommunity || community }}</span>
            </el-form-item>
            <el-form-item>
              <el-button v-if="isOrgAdmin" type="primary" @click="loadCommunityDetail" :loading="detailLoading">查询</el-button>
              <el-button @click="exportData('community')">导出</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <div v-loading="detailLoading">
          <!-- 老人统计卡片 -->
          <el-row :gutter="16" style="margin-bottom: 20px">
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value" style="color: #409EFF">{{ detail.elderly?.total || 0 }}</div>
                <div class="stat-label">在院老人</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value" style="color: #67C23A">{{ detail.elderly?.assigned || 0 }}</div>
                <div class="stat-label">已签约</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value" style="color: #E6A23C">{{ detail.elderly?.unassigned || 0 }}</div>
                <div class="stat-label">待分配</div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-value" style="color: #F56C6C">{{ detail.warnings?.pending || 0 }}</div>
                <div class="stat-label">待处理预警</div>
              </el-card>
            </el-col>
          </el-row>

          <!-- 图表行 -->
          <el-row :gutter="16" style="margin-bottom: 20px">
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header><span style="font-weight: bold">年龄分布</span></template>
                <div ref="ageChartRef" style="height: 300px"></div>
                <el-empty v-if="isEmptyAgeDist" description="暂无数据" :image-size="60" />
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header><span style="font-weight: bold">医生工作量</span></template>
                <div ref="doctorChartRef" style="height: 300px"></div>
                <el-empty v-if="!detail.doctorWorkload?.length" description="暂无数据" :image-size="60" />
              </el-card>
            </el-col>
          </el-row>

          <!-- 趋势图行 -->
          <el-row :gutter="16" style="margin-bottom: 20px">
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header><span style="font-weight: bold">近30天新增老人</span></template>
                <div ref="admissionChartRef" style="height: 280px"></div>
                <el-empty v-if="!detail.admissionTrend?.length" description="暂无数据" :image-size="60" />
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header><span style="font-weight: bold">近7天预警趋势</span></template>
                <div ref="warningChartRef" style="height: 280px"></div>
                <el-empty v-if="!detail.warningTrend?.length" description="暂无数据" :image-size="60" />
              </el-card>
            </el-col>
          </el-row>

          <!-- 设备与预警汇总 -->
          <el-row :gutter="16">
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header><span style="font-weight: bold">设备状态</span></template>
                <el-descriptions :column="2" border size="small" v-if="detail.devices?.total">
                  <el-descriptions-item label="在线">{{ detail.devices.online || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="离线">{{ detail.devices.offline || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="维修中">{{ detail.devices.repairing || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="已报废">{{ detail.devices.scrapped || 0 }}</el-descriptions-item>
                  <el-descriptions-item label="合计">{{ detail.devices.total || 0 }}</el-descriptions-item>
                </el-descriptions>
                <el-empty v-else description="暂无设备数据" :image-size="60" />
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header><span style="font-weight: bold">30天健康概览</span></template>
                <el-descriptions :column="2" border size="small" v-if="detail.healthOverview?.avg_systolic">
                  <el-descriptions-item label="平均收缩压">{{ detail.healthOverview.avg_systolic }} mmHg</el-descriptions-item>
                  <el-descriptions-item label="平均舒张压">{{ detail.healthOverview.avg_diastolic }} mmHg</el-descriptions-item>
                  <el-descriptions-item label="平均心率">{{ detail.healthOverview.avg_heart_rate }} bpm</el-descriptions-item>
                  <el-descriptions-item label="平均血糖">{{ detail.healthOverview.avg_blood_sugar }} mmol/L</el-descriptions-item>
                  <el-descriptions-item label="平均血氧">{{ detail.healthOverview.avg_blood_oxygen }}%</el-descriptions-item>
                  <el-descriptions-item label="已测量老人">{{ detail.healthOverview.measured_elderly }} 人</el-descriptions-item>
                </el-descriptions>
                <el-empty v-else description="暂无健康数据" :image-size="60" />
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getStorage } from '@/utils/localStorage.js'
import { getOverview, getCommunityDetail, downloadOverviewExcel, downloadCommunityDetailExcel } from '@/api/reports.js'
import * as echarts from 'echarts'

// ===== 状态 =====
const activeTab = ref('overview')
const selectedCommunity = ref('')
const overviewLoading = ref(false)
const detailLoading = ref(false)

const overview = reactive({ totals: {}, communities: [], elderlyByCommunity: [], devicesByCommunity: [] })
const detail = reactive({
  elderly: {}, doctorWorkload: [], devices: {}, warnings: {},
  admissionTrend: [], warningTrend: [], healthOverview: {}
})

// ECharts 容器 refs
const elderlyChartRef = ref(null)
const deviceChartRef = ref(null)
const ageChartRef = ref(null)
const doctorChartRef = ref(null)
const admissionChartRef = ref(null)
const warningChartRef = ref(null)

let elderlyChart, deviceChart, ageChart, doctorChart, admissionChart, warningChart

// ===== 角色判断 =====
const roleCode = computed(() => getStorage('RoleCode') || '')
const isOrgAdmin = computed(() => roleCode.value === 'ORG_ADMIN')
const isComAdmin = computed(() => roleCode.value === 'COM_ADMIN')
const community = computed(() => getStorage('Community') || '')

// ===== 社区对比表格数据 =====
const communityTable = computed(() => {
  const map = {}
  // 合并所有社区维度的数据
  overview.elderlyByCommunity?.forEach(e => {
    const c = e.community; if (!map[c]) map[c] = { community: c, elderly: 0, doctors: 0, devices: 0, online: 0, warnings: 0 }
    map[c].elderly = e.cnt
  })
  overview.doctorsByCommunity?.forEach(e => {
    const c = e.community; if (!map[c]) map[c] = { community: c, elderly: 0, doctors: 0, devices: 0, online: 0, warnings: 0 }
    map[c].doctors = e.cnt
  })
  overview.devicesByCommunity?.forEach(e => {
    const c = e.community; if (!map[c]) map[c] = { community: c, elderly: 0, doctors: 0, devices: 0, online: 0, warnings: 0 }
    map[c].devices = e.total; map[c].online = e.online
  })
  overview.warningsByCommunity?.forEach(e => {
    const c = e.community; if (!map[c]) map[c] = { community: c, elderly: 0, doctors: 0, devices: 0, online: 0, warnings: 0 }
    map[c].warnings = e.total
  })
  return Object.values(map)
})

const isEmptyAgeDist = computed(() => {
  const d = detail.elderly?.ageDistribution
  if (!d) return true
  return !(d.lt60 || d.s60 || d.s70 || d.s80 || d.s90)
})

// ===== 加载数据 =====
const loadOverview = async () => {
  overviewLoading.value = true
  try {
    const res = await getOverview()
    if (res.code === 200) Object.assign(overview, res.data)
  } finally {
    overviewLoading.value = false
    await nextTick()
    renderOverviewCharts()
  }
}

const loadCommunityDetail = async () => {
  if (!selectedCommunity.value) {
    ElMessage.warning('请选择社区')
    return
  }
  detailLoading.value = true
  try {
    const res = await getCommunityDetail(selectedCommunity.value)
    if (res.code === 200) {
      Object.keys(detail).forEach(k => delete detail[k])
      Object.assign(detail, res.data)
    }
  } finally {
    detailLoading.value = false
    await nextTick()
    renderDetailCharts()
  }
}

// ===== ECharts 渲染 =====
const renderOverviewCharts = () => {
  // 各社区老人分布 — 柱状图
  if (elderlyChartRef.value && overview.elderlyByCommunity?.length) {
    if (!elderlyChart) elderlyChart = echarts.init(elderlyChartRef.value)
    elderlyChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: overview.elderlyByCommunity.map(e => e.community) },
      yAxis: { type: 'value' },
      series: [{
        name: '老人数', type: 'bar', data: overview.elderlyByCommunity.map(e => e.cnt),
        itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] }
      }]
    }, true)
  }

  // 各社区设备状态 — 堆叠柱状图
  if (deviceChartRef.value && overview.devicesByCommunity?.length) {
    if (!deviceChart) deviceChart = echarts.init(deviceChartRef.value)
    deviceChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['在线', '离线', '维修中'] },
      xAxis: { type: 'category', data: overview.devicesByCommunity.map(e => e.community) },
      yAxis: { type: 'value' },
      series: [
        { name: '在线', type: 'bar', stack: 'total', data: overview.devicesByCommunity.map(e => e.online), itemStyle: { color: '#67C23A' } },
        { name: '离线', type: 'bar', stack: 'total', data: overview.devicesByCommunity.map(e => e.offline), itemStyle: { color: '#F56C6C' } },
        { name: '维修中', type: 'bar', stack: 'total', data: overview.devicesByCommunity.map(e => e.repairing), itemStyle: { color: '#E6A23C' } },
      ]
    }, true)
  }
}

const renderDetailCharts = () => {
  // 年龄分布 — 饼图
  if (ageChartRef.value && detail.elderly?.ageDistribution && !isEmptyAgeDist.value) {
    if (!ageChart) ageChart = echarts.init(ageChartRef.value)
    const d = detail.elderly.ageDistribution
    ageChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: [
          { value: d.lt60 || 0, name: '<60岁' },
          { value: d.s60 || 0, name: '60-69岁' },
          { value: d.s70 || 0, name: '70-79岁' },
          { value: d.s80 || 0, name: '80-89岁' },
          { value: d.s90 || 0, name: '≥90岁' },
        ].filter(x => x.value > 0),
        emphasis: { itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' } }
      }]
    }, true)
  }

  // 医生工作量 — 横向柱状图
  if (doctorChartRef.value && detail.doctorWorkload?.length) {
    if (!doctorChart) doctorChart = echarts.init(doctorChartRef.value)
    doctorChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'value' },
      yAxis: {
        type: 'category',
        data: detail.doctorWorkload.map(d => d.real_name),
        inverse: true
      },
      series: [{
        name: '签约数', type: 'bar',
        data: detail.doctorWorkload.map(d => d.signing_count),
        itemStyle: {
          color: '#409EFF', borderRadius: [0, 4, 4, 0],
        },
        label: { show: true, position: 'right' }
      }]
    }, true)
  }

  // 近30天新增趋势 — 折线图
  if (admissionChartRef.value && detail.admissionTrend?.length) {
    if (!admissionChart) admissionChart = echarts.init(admissionChartRef.value)
    admissionChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: detail.admissionTrend.map(d => d.date) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        name: '新增', type: 'line', data: detail.admissionTrend.map(d => d.cnt),
        smooth: true, itemStyle: { color: '#409EFF' },
        areaStyle: { color: 'rgba(64,158,255,0.1)' }
      }]
    }, true)
  }

  // 近7天预警趋势 — 折线图
  if (warningChartRef.value && detail.warningTrend?.length) {
    if (!warningChart) warningChart = echarts.init(warningChartRef.value)
    warningChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: detail.warningTrend.map(d => d.date) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        name: '预警', type: 'line', data: detail.warningTrend.map(d => d.cnt),
        smooth: true, itemStyle: { color: '#F56C6C' },
        areaStyle: { color: 'rgba(245,108,108,0.1)' }
      }]
    }, true)
  }
}

// ===== 交互 =====
const onTabChange = (tab) => {
  if (tab === 'overview' && !overview.communities?.length) loadOverview()
}

const viewCommunity = (com) => {
  selectedCommunity.value = com
  activeTab.value = 'community'
  loadCommunityDetail()
}

const exportData = async (type) => {
  try {
    let blob
    if (type === 'overview') {
      blob = await downloadOverviewExcel()
    } else {
      blob = await downloadCommunityDetailExcel(selectedCommunity.value)
    }
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `报表导出_${type}_${new Date().toISOString().slice(0, 10)}.xlsx`
    link.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}

// ===== 窗口 resize 自适应 =====
const handleResize = () => {
  elderlyChart?.resize(); deviceChart?.resize()
  ageChart?.resize(); doctorChart?.resize()
  admissionChart?.resize(); warningChart?.resize()
}

onMounted(() => {
  // 处理从 Dashboard 点"详情"传入的社区名
  const viewCom = localStorage.getItem('_viewCommunity')
  if (viewCom) {
    localStorage.removeItem('_viewCommunity')
    selectedCommunity.value = viewCom
    activeTab.value = 'community'
    loadCommunityDetail()
  }

  // 社区管理员：默认进社区详情 + 锁本社区
  if (isComAdmin.value) {
    if (community.value) selectedCommunity.value = community.value
    activeTab.value = 'community'
    if (selectedCommunity.value) loadCommunityDetail()
    return  // 不加载全局总览
  }

  loadOverview()
  window.addEventListener('resize', handleResize)
})
</script>

<style scoped>
.stat-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s;
}
.stat-card:hover { transform: translateY(-2px); }
.stat-value { font-size: 32px; font-weight: bold; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
</style>
