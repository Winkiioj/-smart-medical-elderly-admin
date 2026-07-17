<template>
  <div class="assign-page">
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- ===== 标签1：待分配老人 ===== -->
        <el-tab-pane label="待分配老人" name="unassigned">
          <el-row :gutter="12" align="middle" style="margin-bottom: 16px">
            <el-col :span="6">
              <el-input v-model="kw1" placeholder="搜索姓名或手机号" clearable @keyup.enter="fetchUnassigned" />
            </el-col>
            <el-col :span="4">
              <el-button type="primary" @click="fetchUnassigned">搜索</el-button>
            </el-col>
            <el-col :span="14" style="text-align: right">
              <el-select v-model="batchDoctorId" placeholder="选择目标医生" clearable style="width: 200px" @change="onBatchDoctorChange">
                <el-option v-for="d in doctorOptions" :key="d.id"
                  :label="d.realName + ' (已签' + d.signingCount + '人)'" :value="d.id" />
              </el-select>
              <el-button type="warning" :disabled="selectedIds.length === 0 || !batchDoctorId"
                style="margin-left: 12px" @click="handleBatchAssign">
                批量分配 ({{ selectedIds.length }})
              </el-button>
            </el-col>
          </el-row>

          <el-table :data="unassignedList" v-loading="loading1" stripe border
            @selection-change="onSelectionChange" ref="table1">
            <el-table-column type="selection" width="45" />
            <el-table-column prop="name" label="姓名" width="80" />
            <el-table-column label="性别" width="60">
              <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
            </el-table-column>
            <el-table-column prop="age" label="年龄" width="60" />
            <el-table-column prop="id_card_masked" label="身份证号" width="180" />
            <el-table-column prop="address" label="住址" min-width="140" show-overflow-tooltip />
            <el-table-column prop="admission_date" label="入档日期" width="110" />
            <el-table-column label="分配医生" width="240">
              <template #default="{ row }">
                <el-select placeholder="选择医生" size="small" style="width: 160px"
                  @change="(id) => handleAssign(row.id, id)">
                  <el-option v-for="d in doctorOptions" :key="d.id"
                    :label="d.realName + ' (' + d.signingCount + '人)'" :value="d.id" />
                </el-select>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap" v-if="uTotal > 10">
            <el-pagination background layout="total, prev, pager, next" :total="uTotal"
              :page-size="10" v-model:current-page="uPage" @current-change="fetchUnassigned" />
          </div>
        </el-tab-pane>

        <!-- ===== 标签2：已分配老人 ===== -->
        <el-tab-pane label="已分配" name="assigned">
          <el-row :gutter="12" align="middle" style="margin-bottom: 16px">
            <el-col :span="4">
              <el-select v-model="filterDoctorId" placeholder="按医生筛选" clearable @change="fetchAssigned"
                style="width: 100%">
                <el-option v-for="d in doctorOptions" :key="d.id"
                  :label="d.realName" :value="d.id" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-input v-model="kw2" placeholder="搜索姓名或手机号" clearable @keyup.enter="fetchAssigned" />
            </el-col>
            <el-col :span="4">
              <el-button type="primary" @click="fetchAssigned">搜索</el-button>
            </el-col>
          </el-row>

          <el-table :data="assignedList" v-loading="loading2" stripe border>
            <el-table-column prop="name" label="姓名" width="80" />
            <el-table-column label="性别" width="60">
              <template #default="{ row }">{{ row.gender === 1 ? '男' : '女' }}</template>
            </el-table-column>
            <el-table-column prop="age" label="年龄" width="60" />
            <el-table-column prop="doctor_name" label="签约医生" width="90" />
            <el-table-column prop="admission_date" label="入档日期" width="110" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-select placeholder="调整分配" size="small" style="width: 150px"
                  @change="(newId) => handleReassign(row, newId)">
                  <el-option v-for="d in doctorOptions.filter(x => x.id !== row.doctor_id)"
                    :key="d.id" :label="d.realName + ' (' + d.signingCount + '人)'" :value="d.id" />
                </el-select>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination-wrap" v-if="aTotal > 10">
            <el-pagination background layout="total, prev, pager, next" :total="aTotal"
              :page-size="10" v-model:current-page="aPage" @current-change="fetchAssigned" />
          </div>
        </el-tab-pane>

        <!-- ===== 标签3：医生负荷 ===== -->
        <el-tab-pane label="医生负荷" name="load">
          <el-table :data="doctorLoad" stripe border>
            <el-table-column prop="real_name" label="姓名" width="100" />
            <el-table-column prop="phone" label="手机号" width="140" />
            <el-table-column label="当前签约数" width="120" align="center">
              <template #default="{ row }">
                <el-tag :type="row.signing_count >= 300 ? 'danger' : row.signing_count >= 200 ? 'warning' : 'success'">
                  {{ row.signing_count }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

const activeTab = ref('unassigned')

// ===== 医生下拉选项（三个标签共用） =====
const doctorOptions = ref([])

const fetchDoctorOptions = async () => {
  try {
    const res = await request({ url: '/api/doctor/options', method: 'get' })
    doctorOptions.value = res.data
  } catch (err) { /* ignore */ }
}

// ===== 待分配 =====
const unassignedList = ref([])
const loading1 = ref(false)
const uPage = ref(1), uTotal = ref(0), kw1 = ref('')
const selectedIds = ref([])
const batchDoctorId = ref(null)

const fetchUnassigned = async () => {
  loading1.value = true
  try {
    const res = await request({
      url: '/api/assign/unassigned',
      method: 'get',
      params: { pageNo: uPage.value, pageSize: 10, keyword: kw1.value },
    })
    unassignedList.value = res.data.records
    uTotal.value = res.total
  } catch (err) { /* ignore */ }
  finally { loading1.value = false }
}

const onSelectionChange = (rows) => { selectedIds.value = rows.map(r => r.id) }

const onBatchDoctorChange = () => { /* placeholder */ }

const handleAssign = async (elderlyId, doctorId) => {
  try {
    await request({
      url: '/api/assign/assign',
      method: 'put',
      data: { elderlyId, doctorId },
    })
    ElMessage.success('分配成功')
    fetchUnassigned()
    fetchDoctorOptions()  // 刷新医生签约数
  } catch (err) { /* ignore */ }
}

const handleBatchAssign = async () => {
  try {
    await ElMessageBox.confirm(
      `确认将 ${selectedIds.value.length} 位老人分配给所选医生？`, '批量分配'
    )
    const res = await request({
      url: '/api/assign/batch-assign',
      method: 'put',
      data: { elderlyIds: selectedIds.value, doctorId: batchDoctorId.value },
    })
    ElMessage.success(`已分配 ${res.data.assigned} 位老人`)
    selectedIds.value = []
    batchDoctorId.value = null
    fetchUnassigned()
    fetchDoctorOptions()
  } catch (err) { /* ignore */ }
}

// ===== 已分配 =====
const assignedList = ref([])
const loading2 = ref(false)
const aPage = ref(1), aTotal = ref(0), kw2 = ref(''), filterDoctorId = ref(null)

const fetchAssigned = async () => {
  loading2.value = true
  try {
    const res = await request({
      url: '/api/assign/assigned',
      method: 'get',
      params: { pageNo: aPage.value, pageSize: 10, doctorId: filterDoctorId.value, keyword: kw2.value },
    })
    assignedList.value = res.data.records
    aTotal.value = res.total
  } catch (err) { /* ignore */ }
  finally { loading2.value = false }
}

const handleReassign = async (row, newDoctorId) => {
  try {
    await ElMessageBox.confirm(
      `确认将 ${row.name} 从 ${row.doctor_name} 调整为新医生？`, '调整分配'
    )
    await request({
      url: '/api/assign/reassign',
      method: 'put',
      data: { elderlyId: row.id, newDoctorId },
    })
    ElMessage.success('调整成功')
    fetchAssigned()
    fetchDoctorOptions()
  } catch (err) { /* ignore */ }
}

// ===== 医生负荷 =====
const doctorLoad = ref([])

const fetchDoctorLoad = async () => {
  try {
    const res = await request({ url: '/api/assign/doctor-load', method: 'get' })
    doctorLoad.value = res.data
  } catch (err) { /* ignore */ }
}

const onTabChange = (tab) => {
  if (tab === 'unassigned') fetchUnassigned()
  else if (tab === 'assigned') fetchAssigned()
  else if (tab === 'load') fetchDoctorLoad()
}

onMounted(() => {
  fetchDoctorOptions()
  fetchUnassigned()
})
</script>

<style scoped>
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
