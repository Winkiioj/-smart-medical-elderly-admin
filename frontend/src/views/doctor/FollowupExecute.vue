<template>
  <div class="followup-execute">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/followup' }">随访管理</el-breadcrumb-item>
      <el-breadcrumb-item>{{ isView ? '查看记录' : '随访执行' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">{{ isView ? '随访记录详情' : '随访执行' }}</h2>

    <el-row :gutter="16" v-loading="loading">
      <!-- 左侧：老人信息 + 健康数据 -->
      <el-col :span="10">
        <!-- 老人信息 -->
        <el-card shadow="hover" style="margin-bottom: 16px">
          <template #header><span style="font-weight:bold">老人信息</span></template>
          <template v-if="elderly">
            <p>姓名：<b>{{ elderly.name }}</b> {{ elderly.gender === 1 ? '男' : '女' }} {{ elderly.age }}岁</p>
            <p>社区：{{ elderly.community || '--' }}</p>
            <p>病史：{{ elderly.medicalHistory || '无' }}</p>
            <p v-if="elderlyTags.length">
              标签：
              <el-tag v-for="t in elderlyTags" :key="t.id" :color="t.color" effect="dark" size="small" style="margin-right:4px">
                {{ t.tagName }}
              </el-tag>
            </p>
          </template>
          <p v-else-if="loading" style="color:#909399">加载中...</p>
          <p v-else style="color:#F56C6C">无法加载老人信息</p>
        </el-card>

        <!-- 近期健康数据 -->
        <el-card shadow="hover">
          <template #header><span style="font-weight:bold">近期健康数据</span></template>
          <el-table v-if="healthRecords.length > 0" :data="healthRecords" size="small" border>
            <el-table-column label="日期" width="100">
              <template #default="{ row }">{{ row.measureDate || '--' }}</template>
            </el-table-column>
            <el-table-column label="收缩压" width="70">
              <template #default="{ row }">{{ row.systolicPressure ?? '--' }}</template>
            </el-table-column>
            <el-table-column label="舒张压" width="70">
              <template #default="{ row }">{{ row.diastolicPressure ?? '--' }}</template>
            </el-table-column>
            <el-table-column label="血糖" width="70">
              <template #default="{ row }">{{ row.bloodSugar ?? '--' }}</template>
            </el-table-column>
            <el-table-column label="心率" width="70">
              <template #default="{ row }">{{ row.heartRate ?? '--' }}</template>
            </el-table-column>
          </el-table>
          <p v-else style="color:#909399;font-size:13px;text-align:center;padding:12px 0">暂无健康数据</p>
        </el-card>
      </el-col>

      <!-- 右侧：随访表单 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <el-form :model="form" label-width="80px" :disabled="isView">
            <el-form-item label="随访日期">
              <el-date-picker v-model="form.followupDate" type="date" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item label="老人状态">
              <el-input v-model="form.elderlyStatus" type="textarea" :rows="2" placeholder="描述老人当前状态" />
            </el-form-item>
            <el-form-item label="干预措施">
              <el-input v-model="form.intervention" type="textarea" :rows="3" placeholder="随访过程中采取的措施" />
            </el-form-item>
            <el-form-item label="随访结果">
              <el-select v-model="form.result" placeholder="请选择" style="width: 200px">
                <el-option value="稳定" /><el-option value="需观察" /><el-option value="建议就医" /><el-option value="已协助转诊" />
              </el-select>
            </el-form-item>
            <el-form-item label="下次随访">
              <el-date-picker v-model="form.nextPlanDate" type="date" value-format="YYYY-MM-DD" />
            </el-form-item>
            <el-form-item>
              <el-button v-if="isView" @click="$router.push('/followup')">返回列表</el-button>
              <el-button v-else type="primary" @click="handleSubmit" :disabled="!form.result">完成随访</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { completePlan, getRecordByPlan, getPlanDetail } from '@/api/followup'
import { getTagsByElderly, getTagList } from '@/api/tag'
import { getHealthList } from '@/api/health'
import { getElderlyDetail } from '@/api/elderly'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const isView = ref(false)
const elderly = ref(null)
const elderlyTags = ref([])
const healthRecords = ref([])

const form = reactive({
  followupDate: new Date().toISOString().slice(0, 10),
  elderlyStatus: '',
  intervention: '',
  result: '',
  nextPlanDate: '',
})

const fetchData = async () => {
  loading.value = true
  try {
    const planId = route.params.id

    // 1. 查计划
    const planRes = await getPlanDetail(planId)
    if (planRes.code !== 200) { ElMessage.error('计划不存在'); router.push('/followup'); return }
    const plan = planRes.data

    // 2. 查已完成的随访记录（无记录时返回 code=200 data=null）
    const recordRes = await getRecordByPlan(planId)
    if (recordRes.code === 200 && recordRes.data) {
      isView.value = true
      const r = recordRes.data
      Object.assign(form, {
        followupDate: r.followupDate || '',
        elderlyStatus: r.elderlyStatus || '',
        intervention: r.intervention || '',
        result: r.result || '',
        nextPlanDate: r.nextPlanDate || '',
      })
    }

    // 3. 查老人详情
    try {
      const elderRes = await getElderlyDetail(plan.elderlyId)
      if (elderRes.code === 200) elderly.value = elderRes.data
    } catch { /* 老人信息加载失败 */ }

    // 4. 查老人标签
    try {
      const [tagRes, allTagsRes] = await Promise.all([
        getTagsByElderly(plan.elderlyId),
        getTagList()
      ])
      if (tagRes.code === 200 && tagRes.data) {
        const allTags = (allTagsRes.code === 200 && allTagsRes.data) ? allTagsRes.data : []
        elderlyTags.value = tagRes.data.map(id => allTags.find(t => t.id === id) || { id, tagName: '#' + id, color: '#909399' })
      }
    } catch { /* 标签加载失败 */ }

    // 5. 查近期健康数据
    try {
      const healthRes = await getHealthList(plan.elderlyId, { pageSize: 5, pageNo: 1 })
      if (healthRes.code === 200) healthRecords.value = (healthRes.data || []).slice(0, 5)
    } catch { /* 健康数据加载失败 */ }
  } catch {
    ElMessage.error('页面加载失败')
  } finally { loading.value = false }
}

const handleSubmit = async () => {
  try {
    const res = await completePlan(route.params.id, { ...form })
    if (res.code === 200) { ElMessage.success('随访完成'); router.push('/followup') }
    else ElMessage.error(res.msg)
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => fetchData())
</script>
