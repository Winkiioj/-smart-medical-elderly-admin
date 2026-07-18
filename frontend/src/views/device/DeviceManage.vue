<template>
  <DeviceLayout>
  <div class="device-manage">
    <!-- 面包屑 -->
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>设备台账</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 标题 -->
    <h2 style="margin: 16px 0">设备台账管理</h2>

    <!-- 筛选栏 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="设备状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="2" />
            <el-option label="维修中" :value="3" />
            <el-option label="已报废" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备类型">
          <el-select v-model="query.deviceType" placeholder="全部" clearable style="width: 130px">
            <el-option label="血压计" value="血压计" />
            <el-option label="血糖仪" value="血糖仪" />
            <el-option label="血氧仪" value="血氧仪" />
            <el-option label="智能腕表" value="智能腕表" />
            <el-option label="体温计" value="体温计" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input v-model="query.keyword" placeholder="设备编号/名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <div style="margin-bottom: 16px; display: flex; justify-content: space-between">
      <el-button type="primary" @click="handleAdd">录入新设备</el-button>
      <span style="color: #909399; font-size: 13px">
        在线 <span style="color: #67C23A; font-weight: bold">{{ stats.online }}</span>
        &nbsp;离线 <span style="color: #F56C6C; font-weight: bold">{{ stats.offline }}</span>
        &nbsp;维修中 <span style="color: #E6A23C; font-weight: bold">{{ stats.repairing }}</span>
        &nbsp;已报废 <span style="color: #909399; font-weight: bold">{{ stats.scrapped }}</span>
      </span>
    </div>

    <!-- 表格 -->
    <el-card shadow="hover">
      <el-table :data="tableData" stripe border v-loading="loading" style="width: 100%">
        <el-table-column prop="deviceNo" label="设备编号" width="180" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="deviceType" label="设备类型" width="100" />
        <el-table-column prop="community" label="所属社区" width="120" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="buyDate" label="购置日期" width="110" />
        <el-table-column label="质保" width="110">
          <template #default="{ row }">
            <el-tag v-if="warrantyTag(row)" :type="warrantyTag(row).type" size="small">
              {{ warrantyTag(row).text }}
            </el-tag>
            <span v-else style="color: #909399">正常</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-select
              v-model="row.status"
              size="small"
              style="width: 100px"
              :disabled="row.status === 4"
              @visible-change="(visible) => { if (visible) row._prevStatus = row.status }"
              @change="(val) => handleStatusChange(row, val, row._prevStatus)"
            >
              <el-option :value="1" label="在线" />
              <el-option :value="2" label="离线" />
              <el-option :value="3" label="维修中" />
              <el-option :value="4" label="已报废" />
            </el-select>
            <el-button size="small" @click="handleDetail(row)">详情</el-button>
            <el-button size="small" type="warning" @click="handleEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="query.pageNo"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="fetchData"
        />
      </div>
    </el-card>

    <!-- 录入/编辑对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.isEdit ? '编辑设备' : '录入新设备'"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="如：智能腕表 AW-200" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="form.deviceType" placeholder="请选择" style="width: 100%">
            <el-option label="血压计" value="血压计" />
            <el-option label="血糖仪" value="血糖仪" />
            <el-option label="血氧仪" value="血氧仪" />
            <el-option label="智能腕表" value="智能腕表" />
            <el-option label="体温计" value="体温计" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备编号" prop="deviceNo">
          <el-input v-model="form.deviceNo" placeholder="留空则自动生成" :disabled="dialog.isEdit" />
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="form.brand" placeholder="生产厂商" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.model" placeholder="设备型号" />
        </el-form-item>
        <el-form-item label="所属社区" prop="community">
          <el-input v-model="form.community" placeholder="如：金牛社区" />
        </el-form-item>
        <el-form-item label="购置日期" prop="buyDate">
          <el-date-picker v-model="form.buyDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="保修截止" prop="warrantyEnd">
          <el-date-picker v-model="form.warrantyEnd" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="设备用途说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
  </DeviceLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDevicePage, getDeviceDetail, addDevice, updateDevice, updateDeviceStatus, getDeviceStats } from '@/api/device'
import DeviceLayout from '@/components/DeviceLayout.vue'

// ===== 数据 =====
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  keyword: '',
  deviceType: '',
  status: '',
  community: '',
})
const stats = reactive({ online: 0, offline: 0, repairing: 0, scrapped: 0 })

// ===== 对话框 =====
const dialog = reactive({ visible: false, isEdit: false })
const formRef = ref(null)
const form = reactive({
  id: null,
  deviceName: '',
  deviceType: '',
  deviceNo: '',
  brand: '',
  model: '',
  community: '',
  buyDate: '',
  warrantyEnd: '',
  remark: '',
  updateTime: null,
  operatorId: 1, // TODO: 从登录用户获取
})

const rules = {
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
  community: [{ required: true, message: '请输入所属社区', trigger: 'blur' }],
  buyDate: [{ required: true, message: '请选择购置日期', trigger: 'change' }],
}

// ===== 方法 =====
const statusText = (s) => ({ 1: '在线', 2: '离线', 3: '维修中', 4: '已报废' }[s] || '未知')
const statusTagType = (s) => {
  const map = { 1: 'success', 2: 'danger', 3: 'warning', 4: 'info' }
  return map[s] || 'info'
}

const warrantyTag = (row) => {
  if (!row.warrantyEnd) return null
  const end = new Date(row.warrantyEnd)
  const now = new Date()
  const days = Math.floor((end - now) / (1000 * 60 * 60 * 24))
  if (days < 0) return { type: 'danger', text: '已过期' }
  if (days <= 30) return { type: 'warning', text: `剩余${days}天` }
  return null
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDevicePage({ ...query })
    if (res.code === 200) {
      tableData.value = res.data
      total.value = res.total
    }
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const res = await getDeviceStats()
    if (res.code === 200) {
      Object.assign(stats, res.data)
    }
  } catch { /* ignore */ }
}

const handleSearch = () => {
  query.pageNo = 1
  fetchData()
}

const handleReset = () => {
  query.keyword = ''
  query.deviceType = ''
  query.status = ''
  query.community = ''
  query.pageNo = 1
  fetchData()
}

const handleAdd = () => {
  dialog.isEdit = false
  resetForm()
  dialog.visible = true
}

const handleEdit = async (row) => {
  dialog.isEdit = true
  try {
    const res = await getDeviceDetail(row.id)
    if (res.code === 200) {
      Object.assign(form, res.data)
    }
  } catch { /* ignore */ }
  dialog.visible = true
}

const handleDetail = async (row) => {
  try {
    const res = await getDeviceDetail(row.id)
    if (res.code === 200) {
      const d = res.data
      ElMessageBox.alert(
        `编号：${d.deviceNo}\n名称：${d.deviceName}\n类型：${d.deviceType}\n` +
        `品牌：${d.brand || '--'}\n型号：${d.model || '--'}\n` +
        `社区：${d.community || '--'}\n状态：${statusText(d.status)}\n` +
        `购置：${d.buyDate || '--'}\n保修截止：${d.warrantyEnd || '--'}\n` +
        `备注：${d.remark || '--'}`,
        '设备详情', { confirmButtonText: '关闭' }
      )
    }
  } catch { /* ignore */ }
}

const handleStatusChange = async (row, newStatus, oldStatus) => {
  // 报废需二次确认
  if (newStatus === 4) {
    try {
      await ElMessageBox.confirm(`确认将设备「${row.deviceName}」设为已报废？报废后不可恢复。`, '报废确认', {
        type: 'warning',
        confirmButtonText: '确认报废',
        cancelButtonText: '取消',
      })
    } catch {
      row.status = oldStatus
      return
    }
  }
  try {
    const res = await updateDeviceStatus(row.id, newStatus, 1)
    if (res.code === 200) {
      ElMessage.success('状态已更新')
      fetchStats()
    } else {
      row.status = oldStatus
      ElMessage.error(res.msg || '状态更新失败')
    }
  } catch {
    row.status = oldStatus
    ElMessage.error('状态更新失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    const res = dialog.isEdit ? await updateDevice({ ...form }) : await addDevice({ ...form })
    if (res.code === 200) {
      ElMessage.success(dialog.isEdit ? '编辑成功' : '录入成功')
      dialog.visible = false
      fetchData()
      fetchStats()
    }
  } catch { /* ignore */ }
}

const resetForm = () => {
  Object.assign(form, {
    id: null, deviceName: '', deviceType: '', deviceNo: '',
    brand: '', model: '', community: '', buyDate: '',
    warrantyEnd: '', remark: '', updateTime: null, operatorId: 1,
  })
}

// ===== 生命周期 =====
onMounted(() => {
  fetchData()
  fetchStats()
})
</script>

<style scoped>
.device-manage {
  padding: 0;
}
.search-form .el-form-item {
  margin-bottom: 0;
}
</style>
