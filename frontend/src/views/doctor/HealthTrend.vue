<template>
  <div class="health-trend">
    <h2>健康数据趋势图</h2>

    <el-form :inline="true" style="margin-bottom:16px">
      <el-form-item label="老人">
        <el-select v-model="elderlyId" filterable placeholder="搜索老人" @change="loadTrend" style="width:220px">
          <el-option v-for="e in elderlyList" :key="e.id" :value="e.id"
            :label="`${e.name} (${e.age}岁)`" />
        </el-select>
      </el-form-item>
      <el-form-item label="指标">
        <el-radio-group v-model="metricType" @change="loadTrend">
          <el-radio-button v-for="m in metrics" :key="m.value" :value="m.value">{{ m.label }}</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="时间">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
          start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD"
          @change="loadTrend" style="width:260px" />
      </el-form-item>
    </el-form>

    <el-card>
      <div ref="chartDom" style="height:420px"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { getElderlyList } from '@/api/elderly.js'
import { getTrend } from '@/api/health.js'
import request from '@/utils/request.js'
import * as echarts from 'echarts'

const doctorId = 3
const elderlyList = ref([]), elderlyId = ref(null)
const metricType = ref('systolic_pressure')
const dateRange = ref([]), chartDom = ref(null)

const metrics = [
  { value: 'systolic_pressure',  label: '收缩压',  unit: 'mmHg' },
  { value: 'diastolic_pressure', label: '舒张压',  unit: 'mmHg' },
  { value: 'heart_rate',         label: '心率',    unit: 'bpm'  },
  { value: 'blood_sugar',        label: '血糖',    unit: 'mmol/L' },
  { value: 'blood_oxygen',       label: '血氧',    unit: '%'    },
  { value: 'weight',             label: '体重',    unit: 'kg'   },
  { value: 'bmi',                label: 'BMI',     unit: ''     },
  { value: 'temperature',        label: '体温',    unit: '℃'   },
]

const currentMetric = computed(() => metrics.find(m => m.value === metricType.value))

onMounted(async () => {
  const { data } = await getElderlyList({ doctorId, page: 1, size: 200 })
  elderlyList.value = data.records
  const end = new Date(), start = new Date(end.getTime() - 30 * 86400000)
  dateRange.value = [fmt(start), fmt(end)]
})

const fmt = d => d.toISOString().substring(0, 10)

const loadTrend = async () => {
  if (!elderlyId.value || !dateRange.value?.length) return

  const metric = currentMetric.value
  let data = []
  let rules = []
  try {
    const [trendRes, ruleRes] = await Promise.all([
      getTrend({ elderlyId: elderlyId.value, metricType: metricType.value,
                 startDate: dateRange.value[0], endDate: dateRange.value[1] }),
      request({ url: '/api/health-record/thresholds?metricType=' + metricType.value, method: 'get' }),
    ])
    data = trendRes.data || []
    rules = (ruleRes && ruleRes.data) || []
  } catch (e) {
    console.error('趋势数据加载失败:', e)
    return
  }

  await nextTick()
  if (!chartDom.value) return

  const c = echarts.init(chartDom.value)

  const markLines = rules.length > 0
    ? rules.map(r => ({
        name: `${r.alertLevelName}: ${r.ruleName} (${r.value}${metric.unit})`,
        yAxis: r.value,
        lineStyle: { color: r.color, type: 'dashed', width: 1.5 },
        label: { formatter: `${r.alertLevelName} ${r.value}${metric.unit}`, position: 'middle' }
      }))
    : []

  c.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: params => {
        const p = params[0]
        const over = rules.filter(r => {
          if (r.direction === 'max') return p.value > r.value
          if (r.direction === 'min') return p.value < r.value
          return false
        })
        const warn = over.length > 0
          ? `<br/>⚠️ 触发${over.map(r => r.alertLevelName).join('、')}预警` : ''
        return `${p.axisValue}<br/>${metric.label}: <b>${p.value} ${metric.unit}</b>${warn}`
      }
    },
    xAxis: {
      type: 'category',
      data: data.map(d => d.date?.substring(5)),
      name: '日期',
    },
    yAxis: {
      type: 'value',
      name: `${metric.label} (${metric.unit})`,
      nameLocation: 'middle', nameGap: 50,
      nameTextStyle: { fontSize: 12 },
      axisLabel: { formatter: v => v + metric.unit },
    },
    series: [{
      type: 'line',
      data: data.map(d => d.value),
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      areaStyle: { color: 'rgba(64,158,255,0.08)' },
      lineStyle: { width: 2.5, color: '#409EFF' },
      itemStyle: { color: '#409EFF' },
      markLine: markLines.length > 0 ? {
        silent: true,
        symbol: 'none',
        data: markLines,
      } : undefined,
    }],
    grid: { left: 70, right: 30, top: 40, bottom: 30 },
  })
}
</script>

<style scoped>
.health-trend { padding: 20px }
h2 { font-size: 18px; color: #303133; margin-bottom: 16px }
</style>
