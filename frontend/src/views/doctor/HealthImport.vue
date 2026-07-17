<template>
  <div class="health-import">
    <h2>导入健康数据</h2>

    <!-- 步骤条 -->
    <el-steps :active="step" align-center style="margin: 24px 0 32px">
      <el-step title="下载模板" description="下载JSON模板并填写数据" />
      <el-step title="上传文件" description="上传填写好的JSON文件" />
      <el-step title="导入完成" description="查看导入结果" />
    </el-steps>

    <!-- 步骤内容 -->
    <el-row :gutter="24" justify="center">
      <!-- Step 1 -->
      <el-col :span="step === 1 ? 12 : 0" v-show="step === 1">
        <el-card shadow="hover">
          <template #header><b>① 下载模板</b></template>
          <p style="color:#606266;margin-bottom:16px">下载JSON模板后，按格式填写老人健康数据即可上传导入。</p>
          <el-button type="primary" @click="downloadTemplate">下载模板文件（.json）</el-button>
          <el-divider />
          <el-button @click="step = 2">已有文件，直接上传 →</el-button>
        </el-card>
      </el-col>

      <!-- Step 2 -->
      <el-col :span="step === 2 ? 12 : 0" v-show="step === 2">
        <el-card shadow="hover">
          <template #header><b>② 上传文件</b></template>
          <el-upload
            drag
            :auto-upload="false"
            accept=".json"
            :on-change="handleImport"
            :show-file-list="false"
            style="width:100%"
          >
            <el-icon :size="56" color="#409EFF"><UploadFilled /></el-icon>
            <div style="margin-top:12px;color:#606266">将 JSON 文件拖到此处，或<em>点击上传</em></div>
            <div style="margin-top:4px;font-size:12px;color:#909399">仅支持 .json 格式文件</div>
          </el-upload>
          <div style="text-align:center;margin-top:12px">
            <el-button @click="step = 1">← 返回下载模板</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- Step 3 -->
      <el-col :span="step === 3 ? 12 : 0" v-show="step === 3">
        <el-card shadow="hover" v-loading="importing">
          <template #header><b>③ 导入完成</b></template>
          <el-descriptions :column="2" border style="margin-bottom:16px">
            <el-descriptions-item label="文件总条数">{{ importResult.totalCount || 0 }}</el-descriptions-item>
            <el-descriptions-item label="成功导入">
              <span style="color:#67C23A;font-weight:bold">{{ importResult.successCount || 0 }} 条</span>
            </el-descriptions-item>
            <el-descriptions-item label="导入失败">
              <span style="color:#F56C6C;font-weight:bold">{{ importResult.failCount || 0 }} 条</span>
            </el-descriptions-item>
            <el-descriptions-item label="触发预警">{{ importResult.warningCount || 0 }} 条</el-descriptions-item>
          </el-descriptions>
          <div v-if="importResult.errors?.length" style="max-height:200px;overflow-y:auto;margin-bottom:12px">
            <el-alert v-for="(e, i) in importResult.errors" :key="i" :title="e" type="error" :closable="false" style="margin-bottom:4px" />
          </div>
          <div style="text-align:center">
            <el-button type="primary" @click="step = 2; importResult.totalCount = 0; importResult.successCount = 0; importResult.failCount = 0; importResult.errors = []">继续导入</el-button>
            <el-button @click="$router.push('/elderly')">返回档案列表</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { importHealthData, getTemplate } from '@/api/health.js'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const step = ref(1)
const doctorId = 3
const importing = ref(false)
const importResult = reactive({ totalCount: 0, successCount: 0, failCount: 0, warningCount: 0, errors: [] })

const downloadTemplate = async () => {
  try {
    const { data } = await getTemplate(doctorId)
    const blob = new Blob([data], { type: 'application/json' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = 'health_template.json'
    a.click()
    step.value = 2
  } catch {
    ElMessage.error('下载模板失败')
  }
}

const handleImport = async (file) => {
  importing.value = true
  step.value = 3
  try {
    const content = await file.raw.text()
    const { data } = await importHealthData({ doctorId, jsonContent: content })
    Object.assign(importResult, data)
  } catch (e) {
    importResult.totalCount = 0
    importResult.successCount = 0
    importResult.failCount = 1
    importResult.warningCount = 0
    importResult.errors = ['导入失败: ' + (e?.response?.data?.msg || e.message)]
  } finally {
    importing.value = false
  }
}
</script>

<style scoped>
.health-import { padding: 20px }
h2 { margin-bottom: 0; font-size: 18px; color: #303133 }
</style>
