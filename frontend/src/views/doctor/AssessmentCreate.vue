<template>
  <div class="assessment-create">
    <el-breadcrumb><el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item><el-breadcrumb-item :to="{ path: '/assessment' }">评估管理</el-breadcrumb-item><el-breadcrumb-item>{{ detail.report?.status===1?'查看报告':'填写评估' }}</el-breadcrumb-item></el-breadcrumb>
    <h2 style="margin:16px 0">{{ detail.report?.status===1?'评估报告详情':'健康评估报告' }}</h2>

    <el-row :gutter="16" v-loading="loading">
      <!-- 评分区 -->
      <el-col :span="16">
        <el-card shadow="hover" v-for="d in detail.dimensions" :key="d.id" style="margin-bottom:16px">
          <template #header><span style="font-weight:bold">{{ d.dimensionName }}（满分 {{ d.maxScore }} 分 · 权重 {{ d.weight }}）</span></template>
          <div style="color:#606266;font-size:13px;margin-bottom:12px;padding:8px 12px;background:#f5f7fa;border-radius:4px;line-height:1.6">
            {{ parseGuide(d.scoringGuide) }}
          </div>

          <!-- 逐项评分表格 -->
          <el-table :data="getItems(d)" border size="small" :show-header="getItems(d).length > 0">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="name" label="评估项目" min-width="200" />
            <el-table-column label="得分" width="160" align="center">
              <template #default="{ row, $index }">
                <el-input-number
                  v-model="itemScoreMap[d.id][$index]"
                  :min="0"
                  :max="getPerItemMax(d)"
                  size="small"
                  controls-position="right"
                  @change="() => onItemChange(d)"
                  :disabled="detail.report?.status === 1"
                />
                <span style="margin-left:4px;color:#909399;font-size:12px">/ {{ getPerItemMax(d) }}</span>
              </template>
            </el-table-column>
          </el-table>

          <!-- 维度小计 -->
          <div style="margin-top:10px;display:flex;justify-content:flex-end;align-items:center;gap:8px">
            维度小计：
            <span :style="{color: getDimSum(d.id) > d.maxScore ? '#F56C6C' : '#303133', fontWeight:'bold', fontSize:'16px'}">
              {{ getDimSum(d.id) }}
            </span>
            <span style="color:#909399">/ {{ d.maxScore }} 分</span>
            <el-tag v-if="getDimSum(d.id) > d.maxScore" type="danger" size="small">超出满分</el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 信息区 -->
      <el-col :span="8">
        <el-card shadow="hover" v-if="detail.elderly" style="margin-bottom:16px">
          <template #header><span style="font-weight:bold">老人信息</span></template>
          <p>姓名：{{ detail.elderly.name }} {{ detail.elderly.gender===1?'男':'女' }} {{ detail.elderly.age }}岁</p>
          <p>社区：{{ detail.elderly.community }}</p>
          <p>病史：{{ detail.elderly.medicalHistory || '无' }}</p>
        </el-card>
        <el-card shadow="hover" style="margin-bottom:16px">
          <template #header><span style="font-weight:bold">评估结果</span></template>
          <p v-if="detail.report?.totalScore">总分：<b>{{ detail.report.totalScore }} / {{ detail.report.fullScore }}</b></p>
          <p v-if="detail.report?.scoreLevel">等级：<el-tag :type="levelTag(detail.report.scoreLevel)">{{ detail.report.scoreLevel }}</el-tag></p>
          <p v-if="!detail.report?.totalScore" style="color:#909399">暂未完成</p>
        </el-card>
      </el-col>
    </el-row>

    <!-- 结论+完成 -->
    <el-card shadow="hover" v-if="detail.report?.status===0" style="margin-top:16px">
      <el-form label-width="80px">
        <el-form-item label="评估结论"><el-input v-model="conclusion" type="textarea" :rows="4" placeholder="请填写评估结论（不少于20字）" maxlength="1000" show-word-limit /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleComplete" :disabled="conclusion.length<20">完成评估</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getReportDetail, saveScores, completeReport } from '@/api/assessment'

const route = useRoute(); const router = useRouter()
const loading = ref(false); const conclusion = ref(''); const scoreMap = reactive({}); const detail = reactive({ report: null, dimensions: [], scores: [], elderly: null })
const itemScoreMap = reactive({})

const levelTag = (v) => ({ '优秀': 'success', '良好': 'primary', '一般': 'warning', '较差': 'danger' }[v] || 'info')

const parseGuide = (g) => {
  try { return JSON.parse(g).desc || '' } catch { return g || '' }
}

const getItems = (d) => {
  try {
    const guide = JSON.parse(d.scoringGuide)
    return (guide.items || []).map(name => ({ name }))
  } catch { return [] }
}

const getPerItemMax = (d) => {
  try {
    const guide = JSON.parse(d.scoringGuide)
    // "每题0-2分" → 2, "每题0-1分" → 1, "数字评分0-10" → 10
    const m1 = (guide.desc || '').match(/每[题项].*?0[−-](\d+)\s*分/)
    if (m1) return parseInt(m1[1])
    const m2 = (guide.desc || '').match(/评分\s*0[−-](\d+)/)
    if (m2) return parseInt(m2[1])
  } catch {}
  // fallback: dimension max / item count (ceil)
  const items = getItems(d)
  return items.length > 0 ? Math.ceil(d.maxScore / items.length) : d.maxScore
}

const getDimSum = (dimId) => (itemScoreMap[dimId] || []).reduce((a, b) => a + b, 0)

const onItemChange = (d) => {
  scoreMap[d.id] = Math.min(getDimSum(d.id), d.maxScore)
  autoSave()
}

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getReportDetail(route.params.id)
    if (res.code === 200) {
      Object.assign(detail, res.data)
      // 初始化逐项评分 + 从后端维度总分还原
      detail.dimensions.forEach(d => {
        const items = getItems(d)
        const len = items.length
        // 查找后端已保存的维度总分
        const saved = (detail.scores || []).find(s => s.dimensionId === d.id)
        const dimScore = saved ? (saved.score || 0) : 0
        // 维度总分均摊到各评分项（已发布报告仅展示，草稿可后续调整）
        if (!itemScoreMap[d.id]) {
          const perItem = len > 0 ? Math.floor(dimScore / len) : 0
          const remainder = len > 0 ? dimScore - perItem * len : 0
          const arr = new Array(len).fill(perItem)
          for (let i = 0; i < remainder; i++) arr[i]++
          itemScoreMap[d.id] = reactive(arr)
        }
        // 同步维度总分到 scoreMap
        scoreMap[d.id] = dimScore
      })
    }
  } finally { loading.value = false }
}

const autoSave = async () => {
  if (detail.report?.status !== 0) return
  const list = detail.dimensions.map(d => ({
    dimensionId: d.id,
    dimensionName: d.dimensionName,
    score: scoreMap[d.id] || 0,
    maxScore: d.maxScore,
  }))
  await saveScores(route.params.id, list)
}

const handleComplete = async () => {
  const res = await completeReport(route.params.id, conclusion.value)
  if (res.code === 200) { ElMessage.success('评估完成'); router.push('/assessment') } else ElMessage.error(res.msg)
}

onMounted(() => fetchDetail())
</script>
