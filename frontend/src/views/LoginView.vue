<template>
  <div class="login-page">
    <!-- ========== 左侧：品牌宣传区 ========== -->
    <div class="login-left">
      <!-- 分层圆弧 -->
      <div class="arch arch-1"></div>
      <div class="arch arch-2"></div>
      <div class="arch arch-3"></div>

      <!-- 竖直建筑线 -->
      <div class="vlines"></div>

      <!-- 斜线分割面 -->
      <div class="diagonal-area"></div>

      <!-- 医疗十字 -->
      <div class="medical-cross">
        <div class="mc-h"></div>
        <div class="mc-v"></div>
      </div>

      <!-- 脉搏波 SVG -->
      <svg class="pulse-wave" width="360" height="60" viewBox="0 0 360 60">
        <polyline
          points="0,30 60,30 80,30 90,6 100,54 110,30 160,30 180,30 190,6 200,54 210,30 300,30 360,30"
          fill="none" stroke="white" stroke-width="2" opacity="0.5"
          stroke-dasharray="600" stroke-dashoffset="600"
        >
          <animate attributeName="stroke-dashoffset" from="600" to="0" dur="2s" fill="freeze" begin="0.3s" />
        </polyline>
      </svg>

      <!-- 主体内容 -->
      <div class="left-content">
        <div class="title-block anim-1">
          <div class="brand">SMECMS</div>
          <h1>智慧医养管理系统</h1>
          <div class="tagline">Smart Medical-Elderly Care Management System</div>
        </div>

        <div class="feature-list">
          <div class="feature-row anim-2">
            <div class="idx">01</div>
            <div class="info">
              <div class="name">健康数据管理</div>
              <div class="desc">社区老人健康信息一站汇集，多维度档案检索</div>
            </div>
          </div>
          <div class="feature-row anim-3">
            <div class="idx">02</div>
            <div class="info">
              <div class="name">实时预警监测</div>
              <div class="desc">智能阈值触发告警，异常指标即时推送</div>
            </div>
          </div>
          <div class="feature-row anim-4">
            <div class="idx">03</div>
            <div class="info">
              <div class="name">签约服务体系</div>
              <div class="desc">医生-老人精准匹配，负荷均衡分配</div>
            </div>
          </div>
          <div class="feature-row anim-5">
            <div class="idx">04</div>
            <div class="info">
              <div class="name">评估与随访</div>
              <div class="desc">标准化评估量表 + 周期性随访追踪</div>
            </div>
          </div>
        </div>

        <div class="footnote anim-5">西南交通大学 · 华迪实训项目</div>
      </div>
    </div>

    <!-- ========== 右侧：登录表单区 ========== -->
    <div class="login-right">
      <div class="login-form">
        <h2>欢迎登录</h2>
        <p class="form-subtitle">请输入您的账号信息</p>

        <el-autocomplete
          v-model="username"
          :fetch-suggestions="queryRecentAccounts"
          :trigger-on-focus="true"
          placeholder="用户名"
          size="large"
          :prefix-icon="User"
          clearable
          :debounce="0"
          popper-class="recent-login-popper"
        />
        <el-input
          v-model="password"
          placeholder="密码"
          type="password"
          size="large"
          show-password
          :prefix-icon="Lock"
          @keyup.enter="handleLogin"
        />

        <!-- 验证码 -->
        <div class="captcha-row">
          <el-input
            v-model="captchaCode"
            placeholder="验证码"
            size="large"
            maxlength="4"
            :prefix-icon="Key"
            @keyup.enter="handleLogin"
          />
          <img
            :src="captchaImage"
            alt="验证码"
            class="captcha-img"
            title="点击刷新验证码"
            @click="refreshCaptcha"
          />
        </div>

        <el-button
          type="primary"
          size="large"
          :loading="loading"
          style="width: 100%"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request.js'
import { setStorage } from '@/utils/localStorage.js'

const router = useRouter()
const username = ref('')
const password = ref('')
const loading = ref(false)

// ===== 最近登录账号 =====
const RECENT_KEY = 'recentAccounts'
const MAX_RECENT = 4

const loadRecentAccounts = () => {
  try {
    const raw = localStorage.getItem(RECENT_KEY)
    return raw ? JSON.parse(raw) : []
  } catch { return [] }
}
const recentAccounts = ref(loadRecentAccounts())

const saveRecentAccount = (name) => {
  let list = loadRecentAccounts()
  // 去重：删掉旧出现的同名
  list = list.filter(a => a !== name)
  // 插入到最前面
  list.unshift(name)
  // 截断到最多 4 个
  if (list.length > MAX_RECENT) list = list.slice(0, MAX_RECENT)
  recentAccounts.value = list
  localStorage.setItem(RECENT_KEY, JSON.stringify(list))
}

const queryRecentAccounts = (queryString, cb) => {
  const results = recentAccounts.value
    .filter(a => a.toLowerCase().includes(queryString.toLowerCase()))
    .map(a => ({ value: a }))
  cb(results)
}

// 验证码
const captchaKey = ref('')
const captchaCode = ref('')
const captchaImage = ref('')

const refreshCaptcha = async () => {
  try {
    const res = await request({ url: '/api/auth/captcha', method: 'get' })
    if (res.code === 200) {
      captchaKey.value = res.data.captchaKey
      captchaImage.value = res.data.captchaImage
    }
  } catch { /* ignore */ }
}
onMounted(() => refreshCaptcha())

// 角色 → 首页路由映射
const ROLE_HOME = {
  ORG_ADMIN: '/dashboard',
  COM_ADMIN: '/dashboard',
  DOCTOR: '/doctor-dashboard',
  DEVICE_ADMIN: '/device-dashboard',
}

const handleLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await request({
      url: '/api/auth/login',
      method: 'post',
      data: {
        username: username.value,
        password: password.value,
        captchaKey: captchaKey.value,
        captchaCode: captchaCode.value,
      },
    })
    const data = res.data
    setStorage('Token', data.token)
    setStorage('UserId', String(data.userId || ''))
    setStorage('RoleCode', data.roleCode || '')
    setStorage('RoleName', data.roleName || '')
    setStorage('RealName', data.realName || '')
    setStorage('Community', data.community || '')
    // 登录成功：保存到最近登录列表
    saveRecentAccount(username.value)
    ElMessage.success(`登录成功！欢迎 ${data.roleName || ''}`)
    const home = ROLE_HOME[data.roleCode] || '/dashboard'
    router.push(home)
  } catch (err) {
    // request.js 已弹窗提示；验证码错误时刷新
    refreshCaptcha()
    captchaCode.value = ''
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== 整体布局 ===== */
.login-page {
  display: flex;
  height: 100vh;
}

/* ===== 左侧：品牌区 ===== */
.login-left {
  flex: 1;
  background: linear-gradient(170deg, #0b1a3b 0%, #132744 30%, #1a3a6e 65%, #2a5298 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* ---- 三层圆弧（左下角） ---- */
.arch {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,0.04);
  pointer-events: none;
}
.arch-1 { width: 900px; height: 900px; bottom: -500px; left: -300px; border-color: rgba(64,158,255,0.06); }
.arch-2 { width: 700px; height: 700px; bottom: -400px; left: -200px; border-color: rgba(64,158,255,0.08); }
.arch-3 { width: 500px; height: 500px; bottom: -300px; left: -100px; border-color: rgba(64,158,255,0.10); }

/* ---- 竖直建筑线 ---- */
.vlines {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.vlines::before, .vlines::after {
  content: '';
  position: absolute;
  top: -10%; bottom: -10%;
  width: 1px;
  background: linear-gradient(180deg, transparent, rgba(255,255,255,0.04) 30%, rgba(255,255,255,0.04) 70%, transparent);
}
.vlines::before { left: 18%; }
.vlines::after  { left: 82%; }

/* ---- 斜线分割面 ---- */
.diagonal-area {
  position: absolute;
  top: -20%; left: -10%;
  width: 60%; height: 140%;
  background: linear-gradient(105deg, rgba(64,158,255,0.04) 0%, transparent 60%);
  transform: skewX(-8deg);
  pointer-events: none;
}

/* ---- 医疗十字 ---- */
.medical-cross {
  position: absolute;
  top: 60px; right: 60px;
  opacity: 0.12;
  pointer-events: none;
}
.medical-cross .mc-h {
  position: absolute;
  width: 80px; height: 16px;
  top: 32px; left: 0;
  background: #fff;
  border-radius: 4px;
}
.medical-cross .mc-v {
  position: absolute;
  width: 16px; height: 80px;
  top: 0; left: 32px;
  background: #fff;
  border-radius: 4px;
}

/* ---- 脉搏波 ---- */
.pulse-wave {
  position: absolute;
  bottom: 80px; left: 50%;
  transform: translateX(-50%);
  opacity: 0.08;
  pointer-events: none;
}

/* ---- 主体内容 ---- */
.left-content {
  position: relative;
  z-index: 2;
  width: 400px;
  display: flex;
  flex-direction: column;
  gap: 32px;
  padding: 0 10px;
}

/* 标题区 */
.title-block {
  padding: 0 8px;
}
.title-block .brand {
  font-size: 11px;
  color: #409eff;
  letter-spacing: 6px;
  text-transform: uppercase;
  margin-bottom: 16px;
}
.title-block h1 {
  font-size: 32px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 4px;
  margin: 0 0 10px 0;
  white-space: nowrap;
}
.title-block .tagline {
  font-size: 12px;
  color: rgba(255,255,255,0.45);
  letter-spacing: 2px;
}

/* 特性列表 */
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 0 8px;
}

.feature-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  border-radius: 10px;
  transition: background .25s, transform .25s;
}
.feature-row:hover {
  background: rgba(255,255,255,0.05);
  transform: translateX(6px);
}

/* 序号圆圈 */
.feature-row .idx {
  width: 38px; height: 38px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  flex-shrink: 0;
  border: 1.5px solid;
}
.feature-row:nth-child(1) .idx { color: #409eff; border-color: rgba(64,158,255,0.35);  background: rgba(64,158,255,0.08); }
.feature-row:nth-child(2) .idx { color: #67c23a; border-color: rgba(103,194,58,0.35);  background: rgba(103,194,58,0.08); }
.feature-row:nth-child(3) .idx { color: #e6a23c; border-color: rgba(230,162,60,0.35);  background: rgba(230,162,60,0.08); }
.feature-row:nth-child(4) .idx { color: #a78bfa; border-color: rgba(167,139,250,0.35); background: rgba(167,139,250,0.08); }

.feature-row:nth-child(1):hover .idx { box-shadow: 0 0 16px rgba(64,158,255,0.4); }
.feature-row:nth-child(2):hover .idx { box-shadow: 0 0 16px rgba(103,194,58,0.4); }
.feature-row:nth-child(3):hover .idx { box-shadow: 0 0 16px rgba(230,162,60,0.4); }
.feature-row:nth-child(4):hover .idx { box-shadow: 0 0 16px rgba(167,139,250,0.4); }

.feature-row .info { flex: 1; min-width: 0; }
.feature-row .name {
  font-size: 15px;
  color: rgba(255,255,255,0.9);
  font-weight: 600;
  margin-bottom: 3px;
}
.feature-row .desc {
  font-size: 11px;
  color: rgba(255,255,255,0.4);
}

/* 底部 */
.footnote {
  text-align: center;
  font-size: 10px;
  color: rgba(255,255,255,0.25);
  letter-spacing: 2px;
}

/* ---- 渐进动画 ---- */
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}
.anim-1 { animation: fadeUp .6s .1s both; }
.anim-2 { animation: fadeUp .6s .2s both; }
.anim-3 { animation: fadeUp .6s .3s both; }
.anim-4 { animation: fadeUp .6s .4s both; }
.anim-5 { animation: fadeUp .6s .5s both; }

/* ===== 右侧：登录表单区 ===== */
.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #dce8f5 0%, #e8f0f8 40%, #f0f4fa 100%);
}
.login-form {
  width: 380px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.login-form h2 {
  font-size: 26px;
  color: #303133;
  margin: 0;
}
.form-subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}
.captcha-row {
  display: flex;
  gap: 10px;
}
.captcha-row .el-input {
  flex: 1;
}
.captcha-img {
  width: 130px;
  height: 40px;
  border-radius: 6px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  transition: border-color .2s;
}
.captcha-img:hover {
  border-color: #409eff;
}

/* 最近登录下拉 */
:deep(.recent-login-popper) .el-autocomplete-suggestion__list {
  padding: 0;
}
:deep(.recent-login-popper) .el-autocomplete-suggestion li {
  padding: 10px 16px;
  font-size: 14px;
  cursor: pointer;
}
:deep(.recent-login-popper) .el-autocomplete-suggestion li:hover {
  background: #ecf5ff;
}
</style>
