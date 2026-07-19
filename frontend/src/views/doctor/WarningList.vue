<template>
  <div class="warning-list">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>预警记录</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">预警记录管理</h2>

    <!-- 筛选栏 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="预警级别">
          <el-select v-model="query.alertLevel" placeholder="全部" clearable style="width:110px">
            <el-option :value="1" label="轻度" />
            <el-option :value="2" label="中度" />
            <el-option :value="3" label="重度" />
          </el-select>
        </el-form-item>
        <el-form-item label="预警状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width:120px">
            <el-option :value="0" label="待处理" />
            <el-option :value="1" label="处理中" />
            <el-option :value="2" label="已完成" />
            <el-option :value="3" label="已关闭" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表 -->
    <el-card shadow="hover">
      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column label="预警级别" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTag(row.alertLevel)" size="small">{{ levelText(row.alertLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertTitle" label="预警标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="triggerValue" label="触发值" width="100" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="触发时间" width="160" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="$router.push(`/warning/${row.id}`)">详情</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getWarningPage } from '@/api/warning'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ pageNo: 1, pageSize: 10, doctorId: 3, alertLevel: '', status: '', alertType: '', startTime: '', endTime: '' })

const levelTag = (v) => ({ 1: 'info', 2: 'warning', 3: 'danger' }[v] || 'info')
const levelText = (v) => ({ 1: '轻度', 2: '中度', 3: '重度' }[v] || '')
const statusTag = (v) => ({ 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }[v] || 'info')
const statusText = (v) => ({ 0: '待处理', 1: '处理中', 2: '已完成', 3: '已关闭' }[v] || '')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getWarningPage({ ...query })
    if (res.code === 200) { tableData.value = res.data; total.value = res.total }
  } finally { loading.value = false }
}

const handleSearch = () => { query.pageNo = 1; fetchData() }
const handleReset = () => {
  Object.assign(query, { pageNo: 1, alertLevel: '', status: '', alertType: '' })
  fetchData()
}

onMounted(() => fetchData())
</script>
