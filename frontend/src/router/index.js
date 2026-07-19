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

  // ===== 后台管理页（共享 LayoutView 布局，按角色过滤菜单） =====
  {
    path: '/',
    component: () => import('@/views/LayoutView.vue'),
    redirect: '/doctor-dashboard',
    children: [
      // ===== A 负责：管理员通用 =====
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '工作台' },
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/org/ReportsView.vue'),
        meta: { title: '报表统计' },
      },
      // ===== A 负责：机构管理员专属 =====
      {
        path: 'communities',
        name: 'CommunityManage',
        component: () => import('@/views/org/CommunityManage.vue'),
        meta: { title: '社区管理' },
      },
      // ===== A 负责：社区管理员专属 =====
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

      // ===== B 负责：医生端 =====
      {
        path: 'doctor-dashboard',
        name: 'DoctorDashboard',
        component: () => import('@/views/doctor/DoctorDashboard.vue'),
        meta: { title: '医生工作台' },
      },
      {
        path: 'elderly',
        name: 'ElderlyList',
        component: () => import('@/views/doctor/ElderlyList.vue'),
        meta: { title: '老人档案' },
      },
      {
        path: 'elderly-new',
        name: 'ElderlyNew',
        component: () => import('@/views/doctor/ElderlyList.vue'),
        meta: { title: '本月新增老人', filter: 'new' },
      },
      {
        path: 'health-import',
        name: 'HealthImport',
        component: () => import('@/views/doctor/HealthImport.vue'),
        meta: { title: '健康导入' },
      },
      {
        path: 'health-trend',
        name: 'HealthTrend',
        component: () => import('@/views/doctor/HealthTrend.vue'),
        meta: { title: '趋势图' },
      },

      // ===== B/C 共用：医生端设备报修 =====
      {
        path: 'device-repair',
        name: 'DeviceRepair',
        component: () => import('@/views/doctor/DeviceRepair.vue'),
        meta: { title: '设备报修' },
      },

      // ===== C 负责：设备管理员 =====
      {
        path: 'device-dashboard',
        name: 'DeviceDashboard',
        component: () => import('@/views/device/Dashboard.vue'),
        meta: { title: '设备Dashboard' },
      },
      {
        path: 'devices',
        name: 'DeviceManage',
        component: () => import('@/views/device/DeviceManage.vue'),
        meta: { title: '设备台账' },
      },
      {
        path: 'tags',
        name: 'TagManage',
        component: () => import('@/views/doctor/TagManage.vue'),
        meta: { title: '标签管理' },
      },

      // ===== C 负责：医生端医疗服务 =====
      {
        path: 'warnings',
        name: 'WarningList',
        component: () => import('@/views/doctor/WarningList.vue'),
        meta: { title: '预警记录' },
      },
      {
        path: 'warning/:id',
        name: 'WarningDetail',
        component: () => import('@/views/doctor/WarningDetail.vue'),
        meta: { title: '预警详情' },
      },
      {
        path: 'followup',
        name: 'FollowupManage',
        component: () => import('@/views/doctor/FollowupManage.vue'),
        meta: { title: '随访管理' },
      },
      {
        path: 'followup/execute/:id',
        name: 'FollowupExecute',
        component: () => import('@/views/doctor/FollowupExecute.vue'),
        meta: { title: '执行随访' },
      },
      {
        path: 'assessment',
        name: 'AssessmentManage',
        component: () => import('@/views/doctor/AssessmentManage.vue'),
        meta: { title: '评估报告' },
      },
      {
        path: 'assessment/create/:id',
        name: 'AssessmentCreate',
        component: () => import('@/views/doctor/AssessmentCreate.vue'),
        meta: { title: '新建评估' },
      },
    ],
  },

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
