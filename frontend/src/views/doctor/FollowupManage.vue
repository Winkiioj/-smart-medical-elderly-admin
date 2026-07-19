<template>
  <div class="followup-manage">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>随访管理</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">随访计划管理</h2>

    <el-card shadow="hover" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="随访类型">
          <el-select v-model="query.followupType" placeholder="全部" clearable style="width: 120px">
            <el-option :value="1" label="电话" /><el-option :value="2" label="上门" /><el-option :value="3" label="门诊" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option :value="0" label="待执行" /><el-option :value="1" label="执行中" />
            <el-option :value="2" label="已完成" /><el-option :value="3" label="已逾期" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="showCreate = true">新建随访计划</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="hover">
      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column prop="elderlyId" label="老人ID" width="80" />
        <el-table-column label="类型" width="80"><template #default="{ row }">{{ typeText(row.followupType) }}</template></el-table-column>
        <el-table-column prop="planDate" label="计划日期" width="120" />
        <el-table-column label="剩余天数" width="110" align="center">
          <template #default="{ row }">
            <template v-if="row.status === 2">--</template>
            <el-tag v-else-if="row.status === 3" type="danger" size="small">已逾期</el-tag>
            <el-tag v-else :type="remainTag(row.planDate)" size="small">{{ remainText(row.planDate) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100"><template #default="{ row }"><el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="随访内容" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span :style="row.followupContent && row.followupContent.startsWith('系统自动生成') ? 'color:#909399;font-style:italic' : ''">{{ row.followupContent || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" type="primary" @click="handleStart(row)">开始</el-button>
            <el-button v-if="row.status === 1" size="small" type="success" @click="handleExecute(row)">执行</el-button>
            <el-button v-if="row.status === 2" size="small" @click="handleExecute(row)">查看</el-button>
            <el-button v-if="row.status === 0" size="small" @click="row._editingDate = true">修改日期</el-button>
            <el-date-picker v-if="row.status === 0 && row._editingDate" v-model="row._newDate" size="small" type="date" value-format="YYYY-MM-DD" style="width:130px;margin-left:4px"
              @change="(v) => handleDateChange(row, v)" @blur="row._editingDate = false" />
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;display:flex;justify-content:flex-end">
        <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="fetchData" />
      </div>
    </el-card>

    <!-- 快速创建弹窗 -->
    <el-dialog v-model="showCreate" title="新建随访计划" width="450px" @open="loadElderlyList">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="80px">
        <el-form-item label="选择老人" prop="elderlyId">
          <el-select v-model="form.elderlyId" placeholder="请选择老人" filterable style="width:100%">
            <el-option v-for="e in elderlyList" :key="e.id" :label="`${e.name}（${e.id}）`" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="随访类型" prop="followupType"><el-select v-model="form.followupType" style="width:100%"><el-option :value="1" label="电话" /><el-option :value="2" label="上门" /><el-option :value="3" label="门诊" /></el-select></el-form-item>
        <el-form-item label="计划日期" prop="planDate"><el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="随访内容"><el-input v-model="form.followupContent" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showCreate=false">取消</el-button><el-button type="primary" @click="handleCreate">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFollowupPlanPage, createPlan, startPlan, updatePlanDate, getElderlyList } from '@/api/followup'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const showCreate = ref(false)
const elderlyList = ref([])
const form = reactive({ elderlyId: null, followupType: 1, planDate: '', followupContent: '', doctorId: 3 })
const rules = { elderlyId: [{ required: true }], followupType: [{ required: true }], planDate: [{ required: true }] }
const query = reactive({ pageNo: 1, pageSize: 10, doctorId: 3, followupType: '', status: '' })

const typeText = (v) => ({ 1: '电话', 2: '上门', 3: '门诊' }[v] || '')
const statusTag = (v) => ({ 0: 'info', 1: 'primary', 2: 'success', 3: 'danger' }[v] || 'info')
const statusText = (v) => ({ 0: '待执行', 1: '执行中', 2: '已完成', 3: '已逾期' }[v] || '')

const remainTag = (dateStr) => {
  if (!dateStr) return 'info'
  const d = calcRemainDays(dateStr)
  if (d < 0) return 'danger'
  if (d === 0) return 'warning'
  if (d <= 3) return 'warning'
  return 'success'
}
const remainText = (dateStr) => {
  if (!dateStr) return '--'
  const d = calcRemainDays(dateStr)
  if (d < 0) return `已逾期 ${-d} 天`
  if (d === 0) return '今天'
  return `还有 ${d} 天`
}

// 用本地日期计算剩余天数，避免 UTC 时差
const calcRemainDays = (dateStr) => {
  const [y, m, d] = dateStr.split('-').map(Number)
  const plan = new Date(y, m - 1, d)
  const today = new Date()
  const localToday = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  return Math.floor((plan - localToday) / 86400000)
}

const fetchData = async () => {
  loading.value = true
  try { const res = await getFollowupPlanPage({ ...query }); if (res.code === 200) { tableData.value = res.data; total.value = res.total } } finally { loading.value = false }
}
const handleSearch = () => { query.pageNo = 1; fetchData() }
const handleReset = () => { Object.assign(query, { pageNo: 1, followupType: '', status: '' }); fetchData() }
const handleCreate = async () => {
  try {
    const res = await createPlan({ ...form })
    if (res.code === 200) { ElMessage.success('创建成功'); showCreate.value = false; fetchData() } else { ElMessage.error(res.msg) }
  } catch { ElMessage.error('创建失败') }
}
const loadElderlyList = async () => {
  try {
    const res = await getElderlyList(3)
    if (res.code === 200) elderlyList.value = res.data || []
  } catch { ElMessage.error('加载老人列表失败') }
}
const handleStart = async (row) => {
  try {
    await startPlan(row.id, 3); ElMessage.success('已开始'); fetchData()
  } catch { ElMessage.error('操作失败') }
}
const handleExecute = (row) => router.push(`/followup/execute/${row.id}`)
const handleDateChange = async (row, newDate) => {
  if (!newDate) return
  const res = await updatePlanDate(row.id, newDate)
  if (res.code === 200) { ElMessage.success('日期已更新'); row.planDate = newDate; fetchData() } else { ElMessage.error(res.msg) }
}
onMounted(() => fetchData())
</script>
