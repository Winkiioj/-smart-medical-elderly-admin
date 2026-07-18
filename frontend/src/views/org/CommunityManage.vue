<template>
  <div class="community-manage">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>社区管理</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">社区管理</h2>

    <!-- 筛选 + 操作栏 -->
    <el-card shadow="hover" style="margin-bottom: 16px">
      <el-form :inline="true">
        <el-form-item label="社区名称">
          <el-input v-model="keyword" placeholder="输入社区名搜索" clearable style="width: 200px" @keyup.enter="fetchPage" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchPage">查询</el-button>
          <el-button type="success" @click="handleAdd">新增社区</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card shadow="hover">
      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="name" label="社区名称" width="150" />
        <el-table-column prop="address" label="地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="联系电话" width="130" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm
              :title="row.status === 1 ? '确定停用该社区？' : '确定启用该社区？'"
              @confirm="handleToggle(row)"
            >
              <template #reference>
                <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'">
                  {{ row.status === 1 ? '停用' : '启用' }}
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNo" :page-size="pageSize"
        :total="total" layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @current-change="fetchPage"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑社区' : '新增社区'" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-width="90px">
        <el-form-item label="社区名称" prop="name">
          <el-input v-model="dialog.form.name" placeholder="如：花园社区" maxlength="50" />
        </el-form-item>
        <el-form-item label="社区地址">
          <el-input v-model="dialog.form.address" placeholder="如：成都市郫都区XX路XX号" maxlength="100" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="dialog.form.contactName" placeholder="社区对接人姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="dialog.form.contactPhone" placeholder="手机号" maxlength="15" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dialog.form.remark" type="textarea" :rows="2" placeholder="备注信息" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="dialog.loading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCommunityPage, addCommunity, updateCommunity, toggleCommunityStatus } from '@/api/community.js'

const keyword = ref('')
const pageNo = ref(1)
const pageSize = ref(10)
const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const formRef = ref(null)

const dialog = reactive({
  visible: false, isEdit: false, loading: false,
  form: { id: null, name: '', address: '', contactName: '', contactPhone: '', remark: '' }
})

const rules = {
  name: [{ required: true, message: '请输入社区名称', trigger: 'blur' }],
}

const resetForm = () => {
  dialog.form = { id: null, name: '', address: '', contactName: '', contactPhone: '', remark: '' }
}

const fetchPage = async () => {
  loading.value = true
  try {
    const res = await getCommunityPage(pageNo.value, pageSize.value, keyword.value)
    if (res.code === 200) {
      tableData.value = res.data || []
      total.value = res.total || 0
    }
  } finally { loading.value = false }
}

const handleAdd = () => {
  resetForm()
  dialog.isEdit = false
  dialog.visible = true
}

const handleEdit = (row) => {
  dialog.isEdit = true
  dialog.form = { ...row }
  dialog.visible = true
}

const submitForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  dialog.loading = true
  try {
    const fn = dialog.isEdit ? updateCommunity : addCommunity
    const res = await fn(dialog.form)
    if (res.code === 200) {
      ElMessage.success(dialog.isEdit ? '编辑成功' : '新增成功')
      dialog.visible = false
      fetchPage()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } finally { dialog.loading = false }
}

const handleToggle = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const res = await toggleCommunityStatus(row.id, newStatus)
  if (res.code === 200) {
    ElMessage.success(newStatus === 0 ? '已停用' : '已启用')
    fetchPage()
  }
}

onMounted(() => fetchPage())
</script>
