<template>
  <DeviceLayout>
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

    <!-- 我的报修记录 -->
    <el-card shadow="hover">
      <template #header><span style="font-weight: bold">我的报修记录</span></template>
      <p style="color: #909399; font-size: 13px; margin-bottom: 12px">
        提示：提交报修后，设备状态将变为"维修中"，设备管理员可在台账列表中查看和处理。
      </p>
    </el-card>
  </div>
  </DeviceLayout>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getDevicePage, submitRepair } from '@/api/device'
import DeviceLayout from '@/components/DeviceLayout.vue'

const formRef = ref(null)
const devices = ref([])
const form = reactive({
  deviceId: null,
  faultType: '',
  faultDescription: '',
  reporterId: 1,      // TODO: 从登录用户获取
  reporterName: '张医生', // TODO: 从登录用户获取
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
  } catch { /* ignore */ }
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
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch {
    ElMessage.error('提交失败，请稍后重试')
  }
}
</script>
