<template>
  <div class="doctor-archive">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>医生档案</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">医生档案查阅</h2>

    <!-- 筛选栏 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <el-form :inline="true" :model="query">
        <el-form-item label="社区">
          <el-select v-model="query.community" placeholder="全部社区" clearable style="width: 160px">
            <el-option v-for="c in communityList" :key="c.id" :label="c.name" :value="c.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="姓名/手机号" clearable style="width: 180px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="query.gender" placeholder="全部" clearable style="width: 90px">
            <el-option :value="1" label="男" />
            <el-option :value="2" label="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 100px">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="禁用" />
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
        <el-table-column label="姓名" width="100">
          <template #default="{ row }">
            {{ row.real_name }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="性别" width="70" align="center">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '--' }}</template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.email || '--' }}</template>
        </el-table-column>
        <el-table-column prop="community" label="所属社区" width="130" />
        <el-table-column label="签约数" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.signing_count >= 200 ? 'danger' : row.signing_count >= 100 ? 'warning' : 'success'" size="small">
              {{ row.signing_count }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="create_time" label="入职日期" width="120" />
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
import request from '@/utils/request.js'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const communityList = ref([])

const query = reactive({
  pageNo: 1,
  pageSize: 10,
  community: '',
  keyword: '',
  gender: '',
  status: '',
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = { ...query, gender: query.gender || undefined, status: query.status || undefined }
    const res = await request({ url: '/api/doctor/archive', method: 'get', params })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.total
    }
  } finally { loading.value = false }
}

const handleSearch = () => { query.pageNo = 1; fetchData() }

const handleReset = () => {
  query.pageNo = 1
  query.keyword = ''
  query.gender = ''
  query.status = ''
  query.community = ''
  fetchData()
}

const loadCommunities = async () => {
  try {
    const res = await request({ url: '/api/community/enabled', method: 'get' })
    if (res.code === 200) communityList.value = res.data || []
  } catch { communityList.value = [] }
}

onMounted(() => {
  loadCommunities()
  fetchData()
})
</script>

<style scoped>
.doctor-archive { padding: 0; }
</style>
