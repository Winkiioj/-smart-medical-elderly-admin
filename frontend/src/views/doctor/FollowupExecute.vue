<template>
  <div class="followup-execute">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/followup' }">随访管理</el-breadcrumb-item>
      <el-breadcrumb-item>随访执行</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">随访执行</h2>

    <el-card shadow="hover">
      <el-form :model="form" label-width="100px">
        <el-form-item label="随访日期"><el-date-picker v-model="form.followupDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="老人状态"><el-input v-model="form.elderlyStatus" type="textarea" :rows="2" placeholder="描述老人当前状态" /></el-form-item>
        <el-form-item label="干预措施"><el-input v-model="form.intervention" type="textarea" :rows="3" placeholder="随访过程中采取的措施" /></el-form-item>
        <el-form-item label="随访结果"><el-select v-model="form.result" placeholder="请选择" style="width: 200px">
          <el-option value="稳定" /><el-option value="需观察" /><el-option value="建议就医" /><el-option value="已协助转诊" /></el-select></el-form-item>
        <el-form-item label="下次随访"><el-date-picker v-model="form.nextPlanDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item><el-button type="primary" @click="handleSubmit" :disabled="!form.result">完成随访</el-button></el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { completePlan } from '@/api/followup'

const route = useRoute()
const router = useRouter()
const form = reactive({
  followupDate: new Date().toISOString().slice(0, 10),
  followupType: 1,
  elderlyStatus: '',
  intervention: '',
  result: '',
  nextPlanDate: '',
})

const handleSubmit = async () => {
  try {
    const res = await completePlan(route.params.id, { ...form })
    if (res.code === 200) { ElMessage.success('随访完成'); router.push('/followup') }
    else ElMessage.error(res.msg)
  } catch { ElMessage.error('操作失败') }
}
</script>
