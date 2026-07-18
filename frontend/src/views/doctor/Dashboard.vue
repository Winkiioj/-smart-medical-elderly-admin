<template>
  <div class="dashboard">
    <h2>我的工作台</h2>

    <!-- 数字卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="5" v-for="c in cards" :key="c.label">
        <el-card shadow="hover" class="stat-card" :body-style="{ padding: '20px' }"
          @click="c.link && $router.push(c.link)">
          <div class="card-row">
            <div class="card-icon" :style="{ background: c.color + '20', color: c.color }">
              <el-icon :size="24"><component :is="c.icon" /></el-icon>
            </div>
            <div class="card-text">
              <div class="card-val" :class="{ 'text-danger': c.danger && c.val > 0 }">
                {{ c.val }}
              </div>
              <div class="card-label">{{ c.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="16">
        <el-card><div class="chart-title">📈 近30天新增老人趋势</div>
          <div ref="trendChart" style="height:280px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card><div class="chart-title">🍩 老人年龄分布</div>
          <div ref="ageChart" style="height:280px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getDashboard } from '@/api/health.js'
import * as echarts from 'echarts'
import { User, Plus, Monitor, Warning, Bell } from '@element-plus/icons-vue'

const cards = reactive([
  { val: 0, label: '负责老人', icon: User,     color: '#409EFF', link: '/elderly',      danger: false },
  { val: 0, label: '本月新增', icon: Plus,     color: '#67C23A', link: '/elderly-new',  danger: false },
  { val: '——', label: '设备在线率', icon: Monitor,  color: '#E6A23C', link: '',             danger: false },
  { val: 0, label: '待处理预警', icon: Warning,  color: '#F56C6C', link: '/warnings',     danger: true  },
  { val: 0, label: '未读消息', icon: Bell,     color: '#909399', link: '',              danger: true  },
])
const trendChart = ref(null)
const ageChart = ref(null)

onMounted(async () => {
  const doctorId = 3
  const { data } = await getDashboard(doctorId)

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
      grid: { left: 50, right: 20, top: 20, bottom: 30 },
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
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
      }],
    })
  }
})
</script>

<style scoped>
.dashboard { padding: 20px }
h2 { font-size: 18px; color: #303133; margin-bottom: 16px }
.stat-row { margin-bottom: 0 }
.stat-card { cursor: pointer; border-radius: 8px; transition: all .2s }
.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,.1) }
.card-row { display: flex; align-items: center; gap: 14px }
.card-icon { width: 48px; height: 48px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0 }
.card-text { flex: 1; min-width: 0 }
.card-val { font-size: 26px; font-weight: 700; color: #303133; line-height: 1.2 }
.card-val.text-danger { color: #F56C6C }
.card-label { font-size: 12px; color: #909399; margin-top: 2px }
.chart-title { font-size: 14px; color: #606266; margin-bottom: 12px; font-weight: 600 }
</style>
