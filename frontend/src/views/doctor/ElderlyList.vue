<template>
  <div class="elderly-list">
    <div class="page-header">
      <h2>{{ title }}</h2>
      <el-button type="primary" @click="openAdd">+ 新增老人</el-button>
    </div>

    <!-- 搜索 -->
    <el-form :inline="true" class="search-bar">
      <el-form-item><el-input v-model="keyword" placeholder="姓名/身份证号" clearable /></el-form-item>
      <el-form-item><el-input v-model="community" placeholder="所属社区" clearable /></el-form-item>
      <el-form-item><el-button type="primary" @click="search">搜索</el-button></el-form-item>
      <el-form-item><el-button @click="keyword='';community='';search()">重置</el-button></el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column label="性别" width="60">
        <template #default="{row}">{{ row.gender === 1 ? '男' : '女' }}</template>
      </el-table-column>
      <el-table-column prop="age" label="年龄" width="60" />
      <el-table-column prop="idCard" label="身份证号" width="180" />
      <el-table-column prop="community" label="所属社区" width="150" />
      <el-table-column prop="admissionDate" label="入档日期" width="120" />
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="{row}">
          <el-button size="small" @click="openDetail(row)">查看</el-button>
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top:16px;justify-content:center" background
      :current-page="page" :page-size="size" :total="total"
      @current-change="p => { page = p; search() }" layout="total, prev, pager, next" />

    <!-- 编辑/新增弹窗 -->
    <el-dialog :title="formTitle" v-model="dialogVisible" width="700px" @close="resetForm">
      <el-form :model="form" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="身份证号"><el-input v-model="form.idCard" @blur="parseIdCard" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="性别">
            <el-select v-model="form.gender"><el-option :value="1" label="男" /><el-option :value="2" label="女" /></el-select>
          </el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="社区"><el-input v-model="form.community" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="住址"><el-input v-model="form.address" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="身高(cm)"><el-input-number v-model="form.height" :min="50" :max="250" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="出生日期"><el-input v-model="form.birthDate" readonly placeholder="身份证自动解析" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="年龄"><el-input v-model="form.age" readonly placeholder="自动计算" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="入档日期"><el-date-picker v-model="form.admissionDate" type="date" value-format="YYYY-MM-DD" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="紧急联系人"><el-input v-model="form.emergencyContact" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="紧急电话"><el-input v-model="form.emergencyPhone" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="病史"><el-input v-model="form.medicalHistory" /></el-form-item></el-col>
        </el-row>
        <!-- 家属信息 -->
        <el-divider>家属联系人</el-divider>
        <div v-for="(c, i) in form.contacts" :key="i" style="display:flex;gap:10px;margin-bottom:8px;align-items:center">
          <el-input v-model="c.name" placeholder="姓名" style="flex:1" />
          <el-select v-model="c.relationship" style="width:100px"><el-option v-for="r in rels" :key="r" :value="r" :label="r" /></el-select>
          <el-input v-model="c.phone" placeholder="电话" style="flex:1" />
          <el-checkbox v-model="c.isEmergency">紧急</el-checkbox>
          <el-button size="small" type="danger" @click="form.contacts.splice(i,1)">删</el-button>
        </div>
        <el-button size="small" @click="form.contacts.push({name:'',relationship:'子女',phone:'',isEmergency:0})">+ 添加家属</el-button>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitForm">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getElderlyList, getElderlyDetail, addElderly, updateElderly } from '@/api/elderly.js'
import { ElMessage } from 'element-plus'

const route = useRoute()
const keyword = ref(''), community = ref(''), page = ref(1), size = ref(20), total = ref(0)
const list = ref([]), loading = ref(false)
const isNewFilter = computed(() => route.meta?.filter === 'new')
const title = computed(() => isNewFilter.value ? '本月新增老人' : '老人档案管理')
const rels = ['配偶','子女','父母','兄弟姐妹','其他']

const dialogVisible = ref(false), isEdit = ref(false), formTitle = ref('')
const form = reactive({ id: null, name: '', idCard: '', gender: 1, phone: '', community: '', address: '', height: null, birthDate: '', age: null, admissionDate: '', emergencyContact: '', emergencyPhone: '', medicalHistory: '', contacts: [] })

const doctorId = 3 // TODO: from Token

const search = async () => {
  loading.value = true
  try {
    const params = { doctorId, keyword: keyword.value, community: community.value, page: page.value, size: size.value }
    if (isNewFilter.value) {
      const now = new Date()
      params.startDate = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
      params.endDate = now.toISOString().substring(0, 10)
    }
    const { data } = await getElderlyList(params)
    list.value = data.records
    total.value = data.total
  } finally { loading.value = false }
}
search()

const parseIdCard = () => {
  const c = form.idCard
  if (c && c.length === 18) {
    form.gender = parseInt(c.charAt(16)) % 2 === 1 ? 1 : 2
    const y = c.substring(6, 10), m = c.substring(10, 12), d = c.substring(12, 14)
    form.birthDate = y + '-' + m + '-' + d
    form.age = new Date().getFullYear() - parseInt(y) - (new Date() < new Date(y + '-' + m + '-' + d) ? 1 : 0)
  }
}
const resetForm = () => { Object.assign(form, { id: null, name: '', idCard: '', gender: 1, phone: '', community: '', address: '', height: null, birthDate: '', age: null, admissionDate: '', emergencyContact: '', emergencyPhone: '', medicalHistory: '', contacts: [] }) }
const openAdd = () => { resetForm(); formTitle.value = '新增老人档案'; isEdit.value = false; dialogVisible.value = true }
const openEdit = async (row) => {
  isEdit.value = true; formTitle.value = '编辑老人档案'
  const { data } = await getElderlyDetail(row.id)
  const contacts = data.contacts || []
  Object.assign(form, data.elderly, { contacts: contacts.map(c => ({ ...c, isEmergency: !!c.isEmergency })) })
  dialogVisible.value = true
}
const openDetail = (row) => openEdit(row)
const submitForm = async () => {
  const payload = { ...form, contacts: form.contacts.map(c => ({ ...c, isEmergency: c.isEmergency ? 1 : 0 })) }
  if (isEdit.value) {
    await updateElderly(form.id, payload)
    ElMessage.success('修改成功')
  } else {
    await addElderly(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false; search()
}
</script>

<style scoped>
.elderly-list { padding: 20px }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px }
.search-bar { margin-bottom: 16px }
</style>
