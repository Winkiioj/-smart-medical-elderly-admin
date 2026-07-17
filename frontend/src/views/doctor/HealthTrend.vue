<template>
  <div class="health-trend">
    <h2>健康数据趋势图</h2>

    <el-form :inline="true" style="margin-bottom:16px">
      <el-form-item label="老人"><el-select v-model="elderlyId" filterable placeholder="搜索老人" @change="loadTrend">
        <el-option v-for="e in elderlyList" :key="e.id" :value="e.id" :label="`${e.name} (${e.idCard?.substring(0,6)}****)`" />
      </el-select></el-form-item>
      <el-form-item label="指标">
        <el-radio-group v-model="metricType" @change="loadTrend">
          <el-radio-button v-for="m in metrics" :key="m.value" :value="m.value">{{ m.label }}</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="时间">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束"
          value-format="YYYY-MM-DD" @change="loadTrend" />
      </el-form-item>
    </el-form>

    <el-card>
      <div ref="chartDom" style="height:400px"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { getElderlyList } from '@/api/elderly.js'
import { getTrend } from '@/api/health.js'
import * as echarts from 'echarts'

const doctorId = 3
const elderlyList = ref([]), elderlyId = ref(null), metricType = ref('systolic_pressure')
const dateRange = ref([]), chartDom = ref(null)

const metrics = [
  { value: 'systolic_pressure', label: '收缩压' },
  { value: 'diastolic_pressure', label: '舒张压' },
  { value: 'heart_rate', label: '心率' },
  { value: 'blood_sugar', label: '血糖' },
  { value: 'blood_oxygen', label: '血氧' },
  { value: 'weight', label: '体重' },
  { value: 'bmi', label: 'BMI' },
]

onMounted(async () => {
  const { data } = await getElderlyList({ doctorId, page: 1, size: 200 })
  elderlyList.value = data.records
  // 默认最近30天
  const end = new Date(), start = new Date(end.getTime() - 30 * 86400000)
  dateRange.value = [formatDate(start), formatDate(end)]
})

const formatDate = d => d.toISOString().substring(0, 10)

const loadTrend = async () => {
  if (!elderlyId.value || !dateRange.value?.length) return
  const { data } = await getTrend({ elderlyId: elderlyId.value, metricType: metricType.value, startDate: dateRange.value[0], endDate: dateRange.value[1] })
  await nextTick()
  if (chartDom.value) {
    const c = echarts.init(chartDom.value)
    c.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(d => d.date.substring(5)) },
      yAxis: { type: 'value' },
      series: [{ type: 'line', data: data.map(d => d.value), smooth: true, areaStyle: { opacity: 0.15 } }],
    })
  }
}
</script>

<style scoped>
.health-trend { padding: 20px }
</style>
