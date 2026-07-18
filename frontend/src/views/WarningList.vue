<template>
  <div class="warning-page">
    <h2>预警记录</h2>
    <el-table :data="list" border stripe v-loading="loading" empty-text="暂无预警记录">
      <el-table-column label="级别" width="80">
        <template #default="{row}">
          <el-tag :type="row.alertLevel===3?'danger':row.alertLevel===2?'warning':'success'" size="small">
            {{ {1:'轻度',2:'中度',3:'重度'}[row.alertLevel] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="alertTitle" label="预警标题" min-width="200" />
      <el-table-column prop="triggerValue" label="触发值" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status===0?'danger':row.status===1?'warning':'info'" size="small">
            {{ {0:'待处理',1:'处理中',2:'已完成',3:'已关闭'}[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="触发时间" width="170">
        <template #default="{row}">{{ row.createTime?.substring(0,16) }}</template>
      </el-table-column>
    </el-table>
    <p style="color:#909399;font-size:12px;margin-top:12px;text-align:center">⚠ 完整预警处理功能由 C 开发</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '@/utils/request.js'

const list = ref([]), loading = ref(false)
onMounted(async () => {
  loading.value = true
  try {
    const { data } = await request({ url: '/api/doctor/warnings?doctorId=3', method: 'get' })
    list.value = data || []
  } finally { loading.value = false }
})
</script>

<style scoped>
.warning-page { padding: 0 }
h2 { font-size: 18px; color: #303133; margin-bottom: 16px }
</style>
