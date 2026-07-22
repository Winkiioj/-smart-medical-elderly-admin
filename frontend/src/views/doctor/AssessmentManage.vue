<template>
  <div class="assessment-manage">
    <el-breadcrumb><el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item><el-breadcrumb-item>评估管理</el-breadcrumb-item></el-breadcrumb>
    <h2 style="margin:16px 0">健康评估报告</h2>
    <div style="margin-bottom:16px">
      <el-button type="primary" @click="showCreate=true">新建评估报告</el-button>
    </div>
    <el-card shadow="hover">
      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column prop="elderlyName" label="老人" width="80" />
        <el-table-column prop="templateName" label="模板" min-width="120" show-overflow-tooltip />
        <el-table-column label="总分" width="90"><template #default="{ row }">{{ row.totalScore || 0 }} / {{ row.fullScore || 0 }}</template></el-table-column>
        <el-table-column label="等级" width="80"><template #default="{ row }"><el-tag :type="levelTag(row.scoreLevel)" size="small">{{ row.scoreLevel || '--' }}</el-tag></template></el-table-column>
        <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'已发布':'草稿' }}</el-tag></template></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column prop="suggestion" label="结论" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">{{ row.suggestion || '--' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status===0" size="small" type="primary" @click="$router.push(`/assessment/create/${row.id}`)">继续评估</el-button>
            <el-button v-else size="small" @click="$router.push(`/assessment/create/${row.id}`)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top:16px;display:flex;justify-content:flex-end">
        <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="fetchData" />
      </div>
    </el-card>
    <el-dialog v-model="showCreate" title="新建评估报告" width="400px" @open="loadDialogData">
      <el-form label-width="80px">
        <el-form-item label="选择老人">
          <el-select v-model="createForm.elderlyId" placeholder="请选择老人" filterable style="width:100%">
            <el-option v-for="e in elderlyList" :key="e.id" :label="`${e.name} (${e.id})`" :value="e.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择模板">
          <el-select v-model="createForm.templateId" placeholder="请选择评估模板" style="width:100%">
            <el-option v-for="t in templateList" :key="t.id" :label="`${t.templateName} (${t.dimensionCount}维度·满分${t.fullScore})`" :value="t.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showCreate=false">取消</el-button><el-button type="primary" @click="handleCreate" :disabled="!createForm.elderlyId||!createForm.templateId">创建</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getReportPage, createReport, getElderlyList, getTemplateList } from '@/api/assessment'
import { getStorage } from '@/utils/localStorage.js'

const router = useRouter()
const loading = ref(false); const tableData = ref([]); const total = ref(0); const showCreate = ref(false)
const elderlyList = ref([]); const templateList = ref([])
const query = reactive({ pageNo: 1, pageSize: 10, doctorId: Number(getStorage('UserId')) })
const createForm = reactive({ elderlyId: null, templateId: null, doctorId: Number(getStorage('UserId')) })

const loadDialogData = async () => {
  const doctorId = Number(getStorage('UserId'))
  const [elderlyRes, tmplRes] = await Promise.all([getElderlyList(doctorId), getTemplateList()])
  if (elderlyRes.code === 200) elderlyList.value = elderlyRes.data || []
  if (tmplRes.code === 200) templateList.value = tmplRes.data || []
}

const levelTag = (v) => ({ '优秀': 'success', '良好': 'primary', '一般': 'warning', '较差': 'danger' }[v] || 'info')

const fetchData = async () => {
  loading.value = true
  try { const res = await getReportPage({ ...query }); if (res.code===200){ tableData.value=res.data; total.value=res.total } } finally { loading.value = false }
}
const handleCreate = async () => {
  const res = await createReport({ ...createForm })
  if (res.code === 200) { showCreate.value = false; router.push('/assessment/create/' + res.data.report.id) } else ElMessage.error(res.msg)
}
onMounted(() => fetchData())
</script>
