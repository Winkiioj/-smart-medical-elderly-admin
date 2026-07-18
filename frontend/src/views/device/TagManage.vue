<template>
  <div class="tag-manage">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>标签管理</el-breadcrumb-item>
    </el-breadcrumb>

    <h2 style="margin: 16px 0">老人标签管理</h2>

    <!-- 标签列表 -->
    <el-card shadow="hover">
      <div style="margin-bottom: 12px">
        <el-button type="primary" @click="handleAdd">新增标签</el-button>
      </div>
      <el-table :data="tags" stripe border v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="标签名" width="120">
          <template #default="{ row }">
            <el-tag :color="row.color" size="small" effect="dark">{{ row.tagName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="color" label="颜色" width="90" />
        <el-table-column prop="description" label="说明" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" type="warning" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑标签' : '新增标签'" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标签名" prop="tagName">
          <el-input v-model="form.tagName" placeholder="如：独居、失能、慢性病" />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-color-picker v-model="form.color" show-alpha />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" placeholder="标签说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTagList, addTag, updateTag, deleteTag } from '@/api/tag'

const loading = ref(false)
const tags = ref([])
const formRef = ref(null)

const dialog = reactive({ visible: false, isEdit: false })
const form = reactive({ id: null, tagName: '', color: '#409EFF', description: '' })

const rules = {
  tagName: [{ required: true, message: '请输入标签名', trigger: 'blur' }],
  color: [{ required: true, message: '请选择颜色', trigger: 'change' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTagList()
    if (res.code === 200) tags.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialog.isEdit = false
  Object.assign(form, { id: null, tagName: '', color: '#409EFF', description: '' })
  dialog.visible = true
}

const handleEdit = (row) => {
  dialog.isEdit = true
  Object.assign(form, row)
  dialog.visible = true
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除标签「${row.tagName}」？`, '确认删除', { type: 'warning' })
  try {
    const res = await deleteTag(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchData()
    }
  } catch { /* 取消 */ }
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    const fn = dialog.isEdit ? updateTag : addTag
    const res = await fn({ ...form })
    if (res.code === 200) {
      ElMessage.success(dialog.isEdit ? '编辑成功' : '新增成功')
      dialog.visible = false
      fetchData()
    }
  } catch { /* ignore */ }
}

onMounted(() => fetchData())
</script>
