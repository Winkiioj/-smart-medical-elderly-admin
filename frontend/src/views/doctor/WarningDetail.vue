<template>
  <div class="warning-detail">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/warnings' }">预警记录</el-breadcrumb-item>
      <el-breadcrumb-item>预警详情</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">预警详情</h2>

    <el-row :gutter="16" v-loading="loading">
      <!-- 预警信息 -->
      <el-col :span="14">
        <el-card shadow="hover" style="margin-bottom: 16px">
          <template #header><span style="font-weight: bold">预警信息</span></template>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="预警级别">
              <el-tag :type="levelTag(warning.alertLevel)">{{ levelText(warning.alertLevel) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="预警状态">
              <el-tag :type="statusTag(warning.status)">{{ statusText(warning.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="预警标题" :span="2">{{ warning.alertTitle }}</el-descriptions-item>
            <el-descriptions-item label="触发值">{{ warning.triggerValue }}</el-descriptions-item>
            <el-descriptions-item label="阈值">{{ warning.thresholdValue || '--' }}</el-descriptions-item>
            <el-descriptions-item label="触发时间">{{ warning.createTime }}</el-descriptions-item>
            <el-descriptions-item label="处理人">{{ warning.handlerId || '--' }}</el-descriptions-item>
            <el-descriptions-item label="处理意见" :span="2">{{ warning.handleOpinion || '--' }}</el-descriptions-item>
            <el-descriptions-item label="处理结果">{{ warning.handleResult || '--' }}</el-descriptions-item>
            <el-descriptions-item label="处理时间">{{ warning.handleTime || '--' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 操作区（仅医生可操作） -->
        <template v-if="isDoctor">
          <!-- 待处理 -->
          <el-card shadow="hover" v-if="warning.status === 0">
            <el-button type="primary" @click="handleAccept">接单处理</el-button>
            <el-button type="info" @click="showCloseDialog = true">关闭预警</el-button>
          </el-card>

          <!-- 处理中 -->
          <el-card shadow="hover" v-if="warning.status === 1">
            <el-alert type="warning" :closable="false" show-icon style="margin-bottom: 16px">
              <template #title>已自动生成随访计划（标注"【预警生成】"），请先执行随访</template>
              <template #default>
                <el-button type="primary" size="small" style="margin-top:8px" @click="$router.push('/followup')">
                  前往随访管理
                </el-button>
              </template>
            </el-alert>

            <el-divider />

            <div style="color:#909399;font-size:13px;margin-bottom:12px">
              完成随访后，请在此处填写预警处理结果：
            </div>
            <el-form :inline="true">
              <el-form-item label="处理意见">
                <el-input v-model="opinion" placeholder="填写核实情况和处理建议" style="width: 280px" maxlength="500" />
              </el-form-item>
              <el-form-item label="处理结果">
                <el-select v-model="result" placeholder="请选择" style="width: 180px">
                  <el-option value="已联系并确认就医" label="已联系并确认就医" />
                  <el-option value="已联系确认为设备误差" label="已联系确认为设备误差" />
                  <el-option value="已安排随访跟进" label="已安排随访跟进" />
                  <el-option value="其他" label="其他" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="success" @click="handleComplete">完成处理</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </template>
      </el-col>

      <!-- 老人信息 -->
      <el-col :span="10">
        <el-card shadow="hover" v-if="elderly">
          <template #header><span style="font-weight: bold">老人信息</span></template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="姓名">{{ elderly.name }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ elderly.gender === 1 ? '男' : '女' }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ elderly.age }} 岁</el-descriptions-item>
            <el-descriptions-item label="社区">{{ elderly.community }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ elderly.phone || '--' }}</el-descriptions-item>
            <el-descriptions-item label="紧急联系人">{{ elderly.emergencyContact || '--' }}</el-descriptions-item>
            <el-descriptions-item label="紧急电话">{{ elderly.emergencyPhone || '--' }}</el-descriptions-item>
            <el-descriptions-item label="既往病史">{{ elderly.medicalHistory || '--' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <!-- 关闭预警对话框 -->
    <el-dialog v-model="showCloseDialog" title="关闭预警" width="400px">
      <el-form>
        <el-form-item label="关闭原因">
          <el-select v-model="closeReason" placeholder="请选择" style="width: 100%">
            <el-option value="误报-指标正常波动" label="误报-指标正常波动" />
            <el-option value="误报-设备故障" label="误报-设备故障" />
            <el-option value="误报-测量操作不当" label="误报-测量操作不当" />
            <el-option value="误报-人为误录入" label="误报-人为误录入" />
            <el-option value="其他" label="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCloseDialog = false">取消</el-button>
        <el-button type="primary" @click="handleClose" :disabled="!closeReason">确认关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getWarningDetail, acceptWarning, completeWarning, closeWarning } from '@/api/warning'
import { getStorage } from '@/utils/localStorage.js'

const route = useRoute()
const router = useRouter()
const isDoctor = computed(() => getStorage('RoleCode') === 'DOCTOR')
const loading = ref(false)
const warning = reactive({})
const elderly = ref(null)
const opinion = ref('')
const result = ref('')
const showCloseDialog = ref(false)
const closeReason = ref('')

const levelTag = (v) => ({ 1: 'info', 2: 'warning', 3: 'danger' }[v] || 'info')
const levelText = (v) => ({ 1: '轻度', 2: '中度', 3: '重度' }[v] || '')
const statusTag = (v) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }[v] || 'info')
const statusText = (v) => ({ 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }[v] || '')

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getWarningDetail(route.params.id)
    if (res.code === 200) {
      Object.assign(warning, res.data.warning)
      elderly.value = res.data.elderly
    }
  } finally { loading.value = false }
}

const handleAccept = async () => {
  try {
    const res = await acceptWarning(warning.id, 3) // TODO: 真实 handlerId
    if (res.code === 200) {
      ElMessage.success(res.data || '已接单，随访计划已自动生成')
      fetchDetail()
    } else ElMessage.error(res.msg)
  } catch { ElMessage.error('操作失败') }
}

const handleComplete = async () => {
  if (!opinion.value || !result.value) { ElMessage.warning('请填写处理意见和处理结果'); return }
  try {
    const res = await completeWarning(warning.id, opinion.value, result.value)
    if (res.code === 200) { ElMessage.success('处理完成'); fetchDetail() }
    else ElMessage.error(res.msg)
  } catch { ElMessage.error('操作失败') }
}

const handleClose = async () => {
  try {
    const res = await closeWarning(warning.id, closeReason.value)
    if (res.code === 200) { ElMessage.success('预警已关闭'); showCloseDialog.value = false; fetchDetail() }
    else ElMessage.error(res.msg)
  } catch { ElMessage.error('操作失败') }
}

onMounted(() => fetchDetail())
</script>
