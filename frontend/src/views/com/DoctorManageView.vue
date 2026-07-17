<template>
  <div class="doctor-page">
    <!-- ===== 搜索栏 ===== -->
    <el-card class="search-bar">
      <el-row :gutter="16" align="middle">
        <el-col :span="6">
          <el-input v-model="keyword" placeholder="搜索姓名或手机号" clearable
            @keyup.enter="handleSearch" />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
        <el-col :span="14" style="text-align: right">
          <el-button type="primary" @click="openAddDialog">+ 新增医生</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- ===== 数据表格 ===== -->
    <el-card style="margin-top: 16px">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="real_name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column label="性别" width="70">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="签约数" width="90" align="center">
          <template #default="{ row }">
            <el-tag type="info">{{ row.signing_count }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1"
              @change="(val) => handleToggle(row, val)" />
          </template>
        </el-table-column>
        <el-table-column prop="create_time" label="入职日期" width="120" />
        <el-table-column label="操作" min-width="120">
          <template #default="{ row }">
            <el-button size="small" type="warning" @click="openEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrap">
        <el-pagination background layout="total, prev, pager, next"
          :total="total" :page-size="pageSize"
          v-model:current-page="pageNo" @current-change="fetchList" />
      </div>
    </el-card>

    <!-- ===== 新增/编辑弹窗 ===== -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.realName" placeholder="请输入医生姓名" />
        </el-form-item>
        <el-form-item label="手机号" :required="isAdd">
          <el-input v-model="form.phone" placeholder="请输入手机号" :disabled="!isAdd" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" style="width: 100%">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
            <el-option label="未知" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="选填" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request.js'

// ===== 列表数据 =====
const loading = ref(false)
const tableData = ref([])
const pageNo = ref(1)
const pageSize = ref(10)
const total = ref(0)
const keyword = ref('')

// 查列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await request({
      url: '/api/doctor/list',
      method: 'get',
      params: { pageNo: pageNo.value, pageSize: pageSize.value, keyword: keyword.value },
    })
    // res = { code: 200, data: { records: [...], total: 50 } }
    tableData.value = res.data.records
    total.value = res.total
  } catch (err) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNo.value = 1
  fetchList()
}

const handleReset = () => {
  keyword.value = ''
  pageNo.value = 1
  fetchList()
}

// ===== 启用/禁用 =====
const handleToggle = async (row, val) => {
  const action = val ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(
      `确认${action} ${row.real_name}？
      ${val ? '' : '禁用后该医生的签约老人将回到待分配状态，待处理预警将自动关闭。'}`,
      `${action}医生`
    )
    await request({
      url: '/api/doctor/toggle-status',
      method: 'put',
      data: { doctorId: row.id, status: val ? 1 : 0 },
    })
    ElMessage.success(`${action}成功`)
    fetchList()
  } catch (err) {
    // 取消或失败
  }
}

// ===== 新增/编辑弹窗 =====
const dialogVisible = ref(false)
const isAdd = ref(true)
const editingId = ref(null)

const form = reactive({
  realName: '',
  phone: '',
  gender: 1,
  email: '',
})

const dialogTitle = computed(() => isAdd.value ? '新增医生' : '编辑医生')

const openAddDialog = () => {
  isAdd.value = true
  editingId.value = null
  form.realName = ''
  form.phone = ''
  form.gender = 1
  form.email = ''
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isAdd.value = false
  editingId.value = row.id
  form.realName = row.real_name
  form.phone = row.phone
  form.gender = row.gender
  form.email = row.email || ''
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!form.realName.trim()) {
    ElMessage.warning('请输入姓名')
    return
  }
  if (isAdd.value && !form.phone.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }

  if (isAdd.value) {
    // 新增
    try {
      const res = await request({
        url: '/api/doctor/add',
        method: 'post',
        data: { realName: form.realName, phone: form.phone, gender: form.gender, email: form.email },
      })
      ElMessage.success('新增成功，初始密码：' + res.data.initPassword)
    } catch (err) {
      return  // 错误已由拦截器提示
    }
  } else {
    // 编辑
    try {
      await request({
        url: '/api/doctor/update',
        method: 'put',
        data: { id: editingId.value, realName: form.realName, gender: form.gender, email: form.email },
      })
      ElMessage.success('修改成功')
    } catch (err) {
      return
    }
  }

  dialogVisible.value = false
  fetchList()
}

// ===== 页面加载 =====
onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.search-bar :deep(.el-card__body) {
  padding: 16px 20px;
}
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
