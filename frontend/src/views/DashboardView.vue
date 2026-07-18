<template>
  <div class="dashboard">
    <!-- 统计卡片行 -->
    <el-row :gutter="20">
      <el-col :span="4" v-for="c in cards" :key="c.label">
        <el-card shadow="hover" :body-style="{ padding: '20px', cursor: c.link ? 'pointer' : 'default' }"
          @click="c.link && $router.push(c.link)">
          <div class="stat-card">
            <div class="stat-icon" :style="{ background: c.bg }">
              <el-icon size="28" :color="c.color"><component :is="c.icon" /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-number" :class="{ 'text-danger': c.danger && c.val > 0 }">{{ c.val }}</div>
              <div class="stat-label">{{ c.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表行 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="16">
        <el-card><div class="chart-title">近30天新增老人趋势</div>
          <div ref="trendChart" style="height:240px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card><div class="chart-title">老人年龄分布</div>
          <div ref="ageChart" style="height:240px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { User, Plus, Monitor, WarningFilled, Bell } from '@element-plus/icons-vue'
import request from '@/utils/request.js'
import * as echarts from 'echarts'

const cards = reactive([
  { val: 0, label: '负责老人',     icon: User,          color: '#409EFF', bg: '#e6f7ff', link: '/elderly',      danger: false },
  { val: 0, label: '本月新增',     icon: Plus,          color: '#67C23A', bg: '#f6ffed', link: '/elderly-new',  danger: false },
  { val: '——', label: '设备在线率', icon: Monitor,       color: '#E6A23C', bg: '#fff7e6', link: '',              danger: false },
  { val: 0, label: '待处理预警',   icon: WarningFilled,  color: '#E6A23C', bg: '#fff7e6', link: '/warnings',      danger: true  },
  { val: 0, label: '未读消息',     icon: Bell,          color: '#F56C6C', bg: '#fef0f0', link: '',              danger: true  },
])

const trendChart = ref(null)
const ageChart = ref(null)

onMounted(async () => {
  try {
    const { data } = await request({ url: '/api/doctor/dashboard?doctorId=3', method: 'get' })
    cards[0].val = data.totalElderly ?? 0
    cards[1].val = data.newThisMonth ?? 0
    cards[2].val = data.deviceOnlineRate ?? '——'
    cards[3].val = data.pendingWarnings ?? 0
    cards[4].val = data.unreadMessages ?? 0

    await nextTick()
    // 趋势图
    if (trendChart.value) {
      const c = echarts.init(trendChart.value)
      c.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: data.admissionTrend?.map(d => d.date.substring(5)) },
        yAxis: { type: 'value', minInterval: 1 },
        series: [{ type: 'line', data: data.admissionTrend?.map(d => d.count),
          smooth: true, areaStyle: { color: 'rgba(64,158,255,0.15)' },
          itemStyle: { color: '#409EFF' }, lineStyle: { width: 2 } }],
      })
    }
    // 年龄饼图
    if (ageChart.value) {
      const c = echarts.init(ageChart.value)
      const ages = data.ageDistribution || {}
      c.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
        legend: { bottom: 0 },
        series: [{
          type: 'pie', radius: ['45%', '70%'], center: ['50%', '45%'],
          label: { show: true, formatter: '{b}\n{d}%' },
          data: Object.entries(ages).map(([k, v]) => ({ name: k, value: v })),
        }],
      })
    }
  } catch { /* 接口调用失败时使用默认值 */ }
})
</script>

<style scoped>
.stat-card { display: flex; align-items: center; gap: 16px }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center }
.stat-number { font-size: 28px; font-weight: bold; color: #303133 }
.stat-number.text-danger { color: #F56C6C }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px }
.chart-title { font-size: 14px; color: #606266; margin-bottom: 12px; font-weight: bold }
</style>
