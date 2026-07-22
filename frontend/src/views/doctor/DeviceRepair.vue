<template>
  <div class="device-repair">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>设备报修</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">设备报修</h2>

    <!-- 报修表单 -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 600px">
        <el-form-item label="报修设备" prop="deviceId">
          <el-select v-model="form.deviceId" placeholder="请选择设备" filterable style="width: 100%"
            @visible-change="(v) => v && fetchDevices()">
            <el-option v-for="d in devices" :key="d.id" :label="`${d.deviceNo} — ${d.deviceName}`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障类型" prop="faultType">
          <el-select v-model="form.faultType" placeholder="请选择故障类型" style="width: 100%">
            <el-option label="设备无法开机" value="设备无法开机" />
            <el-option label="数据明显异常" value="数据明显异常" />
            <el-option label="外观损坏" value="外观损坏" />
            <el-option label="电池续航严重下降" value="电池续航严重下降" />
            <el-option label="设备遗失" value="设备遗失" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDescription">
          <el-input v-model="form.faultDescription" type="textarea" :rows="3"
            placeholder="请描述设备的具体故障情况" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="danger" @click="handleSubmit">提交报修</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 报修记录 -->
    <el-card shadow="hover">
      <template #header><span style="font-weight: bold">报修记录</span></template>
      <el-table :data="repairRecords" stripe border v-loading="repairLoading" size="small" style="width:100%">
        <el-table-column prop="deviceNo" label="设备编号" width="180" />
        <el-table-column prop="faultType" label="故障类型" width="150" />
        <el-table-column prop="faultDesc" label="故障描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="reporterName" label="报修人" width="100" />
        <el-table-column prop="repairTime" label="报修时间" width="160" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status===3?'warning':'success'" size="small">{{ row.status===3?'维修中':'已修复' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <p v-if="repairRecords.length===0" style="color:#909399;text-align:center;padding:20px 0">暂无需维修的设备</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDevicePage, submitRepair, getRepairRecords } from '@/api/device'
import { getStorage } from '@/utils/localStorage.js'

const formRef = ref(null)
const devices = ref([])
const repairRecords = ref([])
const repairLoading = ref(false)
const form = reactive({
  deviceId: null,
  faultType: '',
  faultDescription: '',
  reporterId: Number(getStorage('UserId')),
  reporterName: getStorage('RealName') || '医生',
})

const rules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  faultType: [{ required: true, message: '请选择故障类型', trigger: 'change' }],
  faultDescription: [{ required: true, message: '请填写故障描述', trigger: 'blur' }],
}

const fetchDevices = async () => {
  if (devices.value.length > 0) return
  try {
    const res = await getDevicePage({ pageNo: 1, pageSize: 100 })
    if (res.code === 200) devices.value = res.data || []
  } catch { ElMessage.error('加载设备列表失败') }
}

const fetchRepairRecords = async () => {
  repairLoading.value = true
  try {
    const res = await getRepairRecords()
    if (res.code === 200) repairRecords.value = res.data || []
  } catch { /* ignore */ }
  finally { repairLoading.value = false }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const res = await submitRepair({ ...form })
    if (res.code === 200) {
      ElMessage.success('报修已提交，设备状态已变更为维修中')
      Object.assign(form, { deviceId: null, faultType: '', faultDescription: '' })
      formRef.value.resetFields()
      fetchRepairRecords()
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败，请稍后重试')
  }
}

onMounted(() => fetchRepairRecords())
</script>
