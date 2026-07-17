import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue'),
    meta: { title: '智慧医养管理系统' },
  },
  // ===== A 负责：机构管理员 + 社区管理员 =====
  // { path: '/dashboard',  component: () => import('@/views/org/Dashboard.vue') },
  // { path: '/reports',    component: () => import('@/views/org/Reports.vue') },
  // { path: '/doctors',    component: () => import('@/views/com/DoctorManage.vue') },
  // { path: '/assign',     component: () => import('@/views/com/ElderlyAssign.vue') },
  // { path: '/com-reports',component: () => import('@/views/com/CommunityReports.vue') },

  // ===== B 负责：社区医生 — 老人与健康 =====
  { path: '/doctor-dashboard', component: () => import('@/views/doctor/Dashboard.vue'), meta: { title: '工作台' }  },
  { path: '/elderly',         component: () => import('@/views/doctor/ElderlyList.vue'), meta: { title: '老人档案' }  },
  { path: '/elderly-new',     component: () => import('@/views/doctor/ElderlyList.vue'), meta: { title: '本月新增老人', filter: 'new' } },
  { path: '/health-import',   component: () => import('@/views/doctor/HealthImport.vue'), meta: { title: '健康导入' }  },
  { path: '/health-trend',    component: () => import('@/views/doctor/HealthTrend.vue'), meta: { title: '趋势图' }  },

  // ===== C 负责：社区医生 — 医疗服务 + 设备管理 =====
  { path: '/warnings',        component: () => import('@/views/doctor/WarningList.vue'), meta: { title: '预警记录' }  },  // B 临时过渡页
  // { path: '/followup',        component: () => import('@/views/doctor/FollowupManage.vue') },
  // { path: '/assessment',      component: () => import('@/views/doctor/AssessmentManage.vue') },
  // { path: '/device-repair',   component: () => import('@/views/doctor/DeviceRepair.vue') },
  // { path: '/devices',         component: () => import('@/views/device/DeviceManage.vue') },
  // { path: '/device-monitor',  component: () => import('@/views/device/DeviceMonitor.vue') },
  // { path: '/device-status',   component: () => import('@/views/device/DeviceStatus.vue') },

  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫 — 标题设置
router.beforeEach((to, _from, next) => {
  document.title = to.meta.title || '智慧医养管理系统'
  next()
})

export default router
