<template>
  <div class="elderly-list">
    <div class="page-header">
      <h2>{{ title }}</h2>
      <el-button v-if="isDoctor" type="primary" @click="openAdd">+ 新增老人</el-button>
    </div>

    <!-- ===== 多维度筛选栏 ===== -->
    <el-card shadow="never" class="filter-card">
      <el-form :inline="true" :model="filters" class="filter-form">
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="姓名 / 身份证号" clearable style="width:180px" @keyup.enter="search" />
        </el-form-item>
        <el-form-item v-if="!isComAdmin" label="社区">
          <el-input v-model="filters.community" placeholder="社区名称" clearable style="width:140px" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="filters.gender" placeholder="全部" clearable style="width:90px">
            <el-option :value="1" label="男" />
            <el-option :value="2" label="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄段">
          <el-select v-model="filters.ageRange" placeholder="全部" clearable style="width:120px">
            <el-option value="60-69" label="60 - 69 岁" />
            <el-option value="70-79" label="70 - 79 岁" />
            <el-option value="80-89" label="80 - 89 岁" />
            <el-option value="90+"   label="90 岁以上" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部" clearable style="width:90px">
            <el-option :value="1" label="在院" />
            <el-option :value="0" label="离院" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="filters.tagId" placeholder="全部" clearable style="width:130px">
            <el-option v-for="t in tagList" :key="t.id" :value="t.id" :label="t.tagName" />
          </el-select>
        </el-form-item>
        <el-form-item label="入档区间">
          <el-date-picker
            v-model="filters.admissionRange"
            type="daterange"
            range-separator="至"
            start-placeholder="起始"
            end-placeholder="截止"
            value-format="YYYY-MM-DD"
            style="width:240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ===== 表格 ===== -->
    <el-card shadow="never" style="margin-top:12px">
      <el-table :data="list" border stripe v-loading="loading" @selection-change="onSelectChange">
        <el-table-column type="selection" width="45" />
        <el-table-column prop="name" label="姓名" width="90" />
        <el-table-column label="性别" width="55">
          <template #default="{row}">{{ row.gender === 1 ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="55" align="center" />
        <el-table-column prop="idCard" label="身份证号" width="175" />
        <el-table-column label="状态" width="65" align="center">
          <template #default="{row}">{{ row.status === 1 ? '在院' : '离院' }}</template>
        </el-table-column>
        <el-table-column prop="community" label="所属社区" width="130" />
        <el-table-column prop="admissionDate" label="入档日期" width="115" />
        <el-table-column label="标签" min-width="140">
          <template #default="{row}">
            <el-tag
              v-for="t in (elderlyTags[row.id] || [])" :key="t.id"
              :color="t.color" size="small"
              style="margin:1px 2px;color:#fff"
            >{{ t.tagName }}</el-tag>
            <span v-if="!(elderlyTags[row.id] || []).length" style="color:#ccc">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" :width="isDoctor ? 170 : 100">
          <template #default="{row}">
            <el-button size="small" @click="openDetail(row)">查看</el-button>
            <el-button v-if="isDoctor" size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination style="margin-top:16px;justify-content:center" background
        :current-page="page" :page-size="size" :total="total"
        @current-change="p => { page = p; search() }" layout="total, prev, pager, next" />
    </el-card>

    <!-- 编辑/新增弹窗（仅医生） -->
    <el-dialog :title="formTitle" v-model="dialogVisible" width="750px" @close="resetForm">
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

    <!-- 查看详情弹窗（只读，所有角色可用） -->
    <el-dialog title="老人档案详情" v-model="detailVisible" width="700px">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="姓名">{{ detail.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ detail.gender === 1 ? '男' : '女' }}</el-descriptions-item>
          <el-descriptions-item label="年龄">{{ detail.age }} 岁</el-descriptions-item>
          <el-descriptions-item label="身份证号">{{ detail.idCard }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ detail.phone || '--' }}</el-descriptions-item>
          <el-descriptions-item label="社区">{{ detail.community || '--' }}</el-descriptions-item>
          <el-descriptions-item label="住址" :span="2">{{ detail.address || '--' }}</el-descriptions-item>
          <el-descriptions-item label="身高">{{ detail.height ? detail.height + ' cm' : '--' }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ detail.birthDate || '--' }}</el-descriptions-item>
          <el-descriptions-item label="入档日期">{{ detail.admissionDate || '--' }}</el-descriptions-item>
          <el-descriptions-item label="紧急联系人">{{ detail.emergencyContact || '--' }}</el-descriptions-item>
          <el-descriptions-item label="紧急电话">{{ detail.emergencyPhone || '--' }}</el-descriptions-item>
          <el-descriptions-item label="病史" :span="2">{{ detail.medicalHistory || '--' }}</el-descriptions-item>
        </el-descriptions>
        <el-divider>家属联系人</el-divider>
        <el-table :data="detail.contacts || []" border size="small" v-if="(detail.contacts || []).length > 0">
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="relationship" label="关系" />
          <el-table-column prop="phone" label="电话" />
          <el-table-column label="紧急联系人">
            <template #default="{ row }">{{ row.isEmergency ? '是' : '否' }}</template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="无家属信息" :image-size="40" />
      </template>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getElderlyList, getElderlyDetail, addElderly, updateElderly } from '@/api/elderly.js'
import { getTagList, getTagsByElderly } from '@/api/tag.js'
import { ElMessage } from 'element-plus'
import { getStorage } from '@/utils/localStorage.js'

const route = useRoute()
const isDoctor = computed(() => getStorage('RoleCode') === 'DOCTOR')
const isComAdmin = computed(() => getStorage('RoleCode') === 'COM_ADMIN')
const myCommunity = computed(() => getStorage('Community') || '')

// ===== 筛选 =====
const filters = reactive({
  keyword: '', community: '', gender: null, ageRange: '', status: null, tagId: null, admissionRange: null
})
const tagList = ref([])

const resetFilters = () => {
  Object.assign(filters, { keyword: '', community: '', gender: null, ageRange: '', status: null, tagId: null, admissionRange: null })
  search()
}

// ===== 表格与分页 =====
const page = ref(1), size = ref(20), total = ref(0)
const list = ref([]), loading = ref(false)
const isNewFilter = computed(() => route.meta?.filter === 'new')
const title = computed(() => isNewFilter.value ? '本月新增老人' : '老人档案管理')
const elderlyTags = reactive({})

const dialogVisible = ref(false), isEdit = ref(false), formTitle = ref('')
const form = reactive({ id: null, name: '', idCard: '', gender: 1, phone: '', community: '', address: '', height: null, birthDate: '', age: null, admissionDate: '', emergencyContact: '', emergencyPhone: '', medicalHistory: '', contacts: [] })

// 只读详情
const detailVisible = ref(false)
const detail = ref(null)

const doctorId = Number(getStorage('UserId'))

const search = async () => {
  loading.value = true
  try {
    const [ageMin, ageMax] = parseAgeRange(filters.ageRange)
    const params = {
      doctorId, keyword: filters.keyword, community: filters.community,
      gender: filters.gender, ageMin, ageMax,
      status: filters.status, tagId: filters.tagId,
      page: page.value, size: size.value,
    }
    if (filters.admissionRange?.length === 2) {
      params.startDate = filters.admissionRange[0]
      params.endDate   = filters.admissionRange[1]
    }
    if (isNewFilter.value) {
      const now = new Date()
      params.startDate = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().substring(0, 10)
      params.endDate   = now.toISOString().substring(0, 10)
    }
    const { data } = await getElderlyList(params)
    list.value = data.records
    total.value = data.total
    for (const e of data.records) {
      if (!elderlyTags[e.id]) {
        try {
          const res = await getTagsByElderly(e.id)
          elderlyTags[e.id] = (res.data || []).map(tid => tagList.value.find(t => t.id === tid)).filter(Boolean)
        } catch { elderlyTags[e.id] = [] }
      }
    }
  } finally { loading.value = false }
}

const parseAgeRange = (range) => {
  if (!range) return [null, null]
  if (range === '90+') return [90, 200]
  const [lo, hi] = range.split('-').map(Number)
  return [lo, hi]
}

// ===== 表单逻辑 =====
const rels = ['配偶','子女','父母','兄弟姐妹','其他']

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
const openDetail = async (row) => {
  detailVisible.value = true
  try {
    const { data } = await getElderlyDetail(row.id)
    detail.value = data.elderly || data
    if (data.contacts) detail.value = { ...detail.value, contacts: data.contacts }
  } catch { detail.value = null }
}
const openEdit = async (row) => {
  isEdit.value = true; formTitle.value = '编辑老人档案'
  const { data } = await getElderlyDetail(row.id)
  Object.assign(form, data.elderly, { contacts: (data.contacts || []).map(c => ({ ...c, isEmergency: !!c.isEmergency })) })
  dialogVisible.value = true
}
const submitForm = async () => {
  const payload = { ...form, contacts: form.contacts.map(c => ({ ...c, isEmergency: c.isEmergency ? 1 : 0 })) }
  if (isEdit.value) { await updateElderly(form.id, payload); ElMessage.success('修改成功') }
  else { await addElderly(payload); ElMessage.success('新增成功') }
  dialogVisible.value = false; search()
}

const onSelectChange = () => {}

// ===== 初始化 =====
onMounted(async () => {
  if (isComAdmin.value) filters.community = myCommunity.value
  try {
    const res = await getTagList()
    tagList.value = res.data || []
  } catch { tagList.value = [] }
  search()
})

watch(() => route.path, () => { search() })
</script>

<style scoped>
.elderly-list { padding: 20px }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px }
h2 { font-size: 18px; color: #303133; margin: 0 }
.filter-card { margin-bottom: 0 }
.filter-form { margin-bottom: 0 }
.filter-form .el-form-item { margin-bottom: 8px }
</style>
