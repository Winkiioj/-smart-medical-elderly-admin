<template>
  <div class="tag-manage">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>标签管理</el-breadcrumb-item>
    </el-breadcrumb>
    <h2 style="margin: 16px 0">老人标签管理</h2>

    <el-row :gutter="16">
      <!-- 左侧：标签定义 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span style="font-weight:bold">标签定义</span>
              <el-button type="primary" size="small" @click="handleAdd">新增标签</el-button>
            </div>
          </template>
          <el-table :data="tags" stripe border v-loading="loading" size="small">
            <el-table-column label="标签" width="100">
              <template #default="{ row }">
                <el-tag :color="row.color" size="small" effect="dark">{{ row.tagName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="100" />
            <el-table-column label="操作" width="150" align="center">
              <template #default="{ row }">
                <el-button size="small" type="warning" @click="handleEdit(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：为老人分配标签 -->
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header><span style="font-weight:bold">为老人分配标签</span></template>
          <el-form label-width="80px">
            <el-form-item label="选择老人">
              <el-select v-model="selectedElderlyId" placeholder="请选择" filterable clearable style="width: 100%"
                @change="onElderlyChange">
                <el-option v-for="e in elderlyList" :key="e.id" :label="`${e.name} (${e.community})`" :value="e.id" />
              </el-select>
            </el-form-item>
          </el-form>

          <div v-if="selectedElderlyId" v-loading="assignLoading" style="padding: 8px 0">
            <p style="margin-bottom: 8px;color:#606266;font-size:13px">
              已选老人：
              <b>{{ selectedElderly?.name }}</b>
              {{ selectedElderly?.gender === 1 ? '男' : '女' }}
              {{ selectedElderly?.age }}岁
            </p>
            <el-checkbox-group v-model="checkedTagIds">
              <el-checkbox v-for="t in tags" :key="t.id" :label="t.id" style="margin-right:16px;margin-bottom:8px">
                <el-tag :color="t.color" size="small" effect="dark" style="cursor:pointer">{{ t.tagName }}</el-tag>
              </el-checkbox>
            </el-checkbox-group>
            <div v-if="tags.length === 0" style="color:#909399;font-size:13px">暂无标签，请先在左侧新增</div>
          </div>
          <div v-else style="color:#909399;font-size:13px;text-align:center;padding:20px 0">
            请先选择一位老人
          </div>

          <div v-if="selectedElderlyId" style="margin-top: 16px; text-align: right">
            <el-button type="primary" @click="handleSaveTags" :loading="assignLoading">保存</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 标签定义 新增/编辑 弹窗 -->
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
import { getTagList, addTag, updateTag, deleteTag, getTagsByElderly, saveTagsByElderly } from '@/api/tag'
import { getStorage } from '@/utils/localStorage.js'
import { getElderlyList } from '@/api/followup'

const loading = ref(false)
const assignLoading = ref(false)
const tags = ref([])
const formRef = ref(null)
const elderlyList = ref([])
const selectedElderlyId = ref(null)
const selectedElderly = ref(null)
const checkedTagIds = ref([])

const dialog = reactive({ visible: false, isEdit: false })
const form = reactive({ id: null, tagName: '', color: '#409EFF', description: '' })
const rules = {
  tagName: [{ required: true, message: '请输入标签名', trigger: 'blur' }],
  color: [{ required: true, message: '请选择颜色', trigger: 'change' }],
}

// ===== 标签定义 =====
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getTagList()
    if (res.code === 200) tags.value = res.data || []
  } finally { loading.value = false }
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
  try {
    await ElMessageBox.confirm(`确认删除标签「${row.tagName}」？`, '确认删除', { type: 'warning' })
    const res = await deleteTag(row.id)
    if (res.code === 200) { ElMessage.success('删除成功'); fetchData() }
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
  } catch { ElMessage.error('操作失败') }
}

// ===== 老人标签分配 =====
const loadElderlyList = async () => {
  try {
    const res = await getElderlyList(Number(getStorage('UserId')))
    if (res.code === 200) elderlyList.value = res.data || []
  } catch { ElMessage.error('加载老人列表失败') }
}

const onElderlyChange = async (val) => {
  if (!val) return
  selectedElderly.value = elderlyList.value.find(e => e.id === val)
  assignLoading.value = true
  try {
    const res = await getTagsByElderly(val)
    checkedTagIds.value = (res.code === 200 && res.data) ? res.data : []
  } catch { checkedTagIds.value = [] }
  finally { assignLoading.value = false }
}

const handleSaveTags = async () => {
  assignLoading.value = true
  try {
    const res = await saveTagsByElderly(selectedElderlyId.value, checkedTagIds.value)
    if (res.code === 200) ElMessage.success('标签已保存')
    else ElMessage.error(res.msg)
  } catch { ElMessage.error('保存失败') }
  finally { assignLoading.value = false }
}

onMounted(() => { fetchData(); loadElderlyList() })
</script>
