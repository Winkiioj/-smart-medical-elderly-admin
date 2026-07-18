import { createRouter, createWebHistory } from 'vue-router'
import { getStorage } from '@/utils/localStorage.js'

const routes = [
  // ===== 登录页（无布局，独立页面） =====
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录 — 智慧医养管理系统' },
  },

  // ===== 后台管理页（共享 LayoutView 布局） =====
  {
    path: '/',
    component: () => import('@/views/LayoutView.vue'),
    redirect: '/doctor-dashboard',
    children: [
      // ===== A 负责 =====
      // { path: 'dashboard', ... A 未完成角色筛选，暂时屏蔽 },
      {
        path: 'doctors',
        name: 'DoctorManage',
        component: () => import('@/views/com/DoctorManageView.vue'),
        meta: { title: '医生管理' },
      },
      {
        path: 'assign',
        name: 'ElderlyAssign',
        component: () => import('@/views/com/ElderlyAssignView.vue'),
        meta: { title: '老人分配' },
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/org/ReportsView.vue'),
        meta: { title: '报表统计' },
      },

      // ===== B 负责 =====
      { path: 'doctor-dashboard', component: () => import('@/views/doctor/DoctorDashboard.vue'), meta: { title: '医生工作台' } },
      { path: 'elderly',         component: () => import('@/views/doctor/ElderlyList.vue'), meta: { title: '老人档案' }  },
      { path: 'elderly-new',     component: () => import('@/views/doctor/ElderlyList.vue'), meta: { title: '老人档案', filter: 'new' } },
      { path: 'health-import',   component: () => import('@/views/doctor/HealthImport.vue'), meta: { title: '健康导入' }  },
      { path: 'health-trend',    component: () => import('@/views/doctor/HealthTrend.vue'), meta: { title: '趋势图' }  },

      // ===== C 负责 =====
      { path: 'warnings',        component: () => import('@/views/WarningList.vue'), meta: { title: '预警记录' } },  // B 临时过渡页
      // { path: 'followup', ... },
      // { path: 'devices', ... },
    ],
  },
  // ===== A 负责：机构管理员 + 社区管理员 =====
  // { path: '/dashboard',  component: () => import('@/views/org/Dashboard.vue') },
  // { path: '/reports',    component: () => import('@/views/org/Reports.vue') },
  // { path: '/doctors',    component: () => import('@/views/com/DoctorManage.vue') },
  // { path: '/assign',     component: () => import('@/views/com/ElderlyAssign.vue') },
  // { path: '/com-reports',component: () => import('@/views/com/CommunityReports.vue') },


  // ===== C 负责：社区医生 — 医疗服务 + 设备管理 =====
  // { path: '/warnings',        component: () => import('@/views/doctor/WarningList.vue'), meta: { title: '预警记录' } },  // TODO: C 创建 WarningList.vue 后解注释
  // { path: '/followup',        component: () => import('@/views/doctor/FollowupManage.vue') },
  // { path: '/assessment',      component: () => import('@/views/doctor/AssessmentManage.vue') },

  // C 的设备管理布局（临时，等 A 统一框架后合并）
  { path: '/device-dashboard', component: () => import('@/views/device/Dashboard.vue') },
  { path: '/devices',         component: () => import('@/views/device/DeviceManage.vue') },
  { path: '/tags',            component: () => import('@/views/device/TagManage.vue') },
  { path: '/device-repair',   component: () => import('@/views/doctor/DeviceRepair.vue') },

  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404' },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫：未登录 → 跳登录页
router.beforeEach((to, _from, next) => {
  const token = getStorage('Token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    document.title = to.meta.title || '智慧医养管理系统'
    next()
  }
})

export default router
