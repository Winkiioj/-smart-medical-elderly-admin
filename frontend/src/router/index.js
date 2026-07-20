import { createRouter, createWebHistory } from 'vue-router'
import { getStorage } from '@/utils/localStorage.js'

const R = (code) => code  // 角色常量简写
const ORG = R('ORG_ADMIN'), COM = R('COM_ADMIN'), DOC = R('DOCTOR'), DEV = R('DEVICE_ADMIN')

const routes = [
  // ===== 登录页（无布局，独立页面） =====
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录 — 智慧医养管理系统' },
  },

  // ===== 403 无权限 =====
  {
    path: '/forbidden',
    name: 'Forbidden',
    component: () => import('@/views/Forbidden.vue'),
    meta: { title: '403 — 无权限' },
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
        meta: { title: '工作台', roles: [ORG, COM] },
      },
      {
        path: 'reports',
        name: 'Reports',
        component: () => import('@/views/org/ReportsView.vue'),
        meta: { title: '报表统计', roles: [ORG, COM] },
      },
      // ===== A 负责：机构管理员专属 =====
      {
        path: 'communities',
        name: 'CommunityManage',
        component: () => import('@/views/org/CommunityManage.vue'),
        meta: { title: '社区管理', roles: [ORG] },
      },
      // ===== A 负责：社区管理员专属 =====
      {
        path: 'doctors',
        name: 'DoctorManage',
        component: () => import('@/views/com/DoctorManageView.vue'),
        meta: { title: '医生管理', roles: [COM] },
      },
      {
        path: 'assign',
        name: 'ElderlyAssign',
        component: () => import('@/views/com/ElderlyAssignView.vue'),
        meta: { title: '老人分配', roles: [COM] },
      },

      // ===== B 负责：医生端 =====
      {
        path: 'doctor-dashboard',
        name: 'DoctorDashboard',
        component: () => import('@/views/doctor/DoctorDashboard.vue'),
        meta: { title: '医生工作台', roles: [DOC] },
      },
      {
        path: 'elderly',
        name: 'ElderlyList',
        component: () => import('@/views/doctor/ElderlyList.vue'),
        meta: { title: '老人档案', roles: [DOC, ORG, COM] },
      },
      {
        path: 'elderly-new',
        name: 'ElderlyNew',
        component: () => import('@/views/doctor/ElderlyList.vue'),
        meta: { title: '本月新增老人', filter: 'new', roles: [DOC, COM] },
      },
      {
        path: 'health-import',
        name: 'HealthImport',
        component: () => import('@/views/doctor/HealthImport.vue'),
        meta: { title: '健康导入', roles: [DOC] },
      },
      {
        path: 'health-trend',
        name: 'HealthTrend',
        component: () => import('@/views/doctor/HealthTrend.vue'),
        meta: { title: '趋势图', roles: [DOC] },
      },

      // ===== B/C 共用：医生端设备报修 =====
      {
        path: 'device-repair',
        name: 'DeviceRepair',
        component: () => import('@/views/doctor/DeviceRepair.vue'),
        meta: { title: '设备报修', roles: [DOC] },
      },

      // ===== C 负责：设备管理员 =====
      {
        path: 'device-dashboard',
        name: 'DeviceDashboard',
        component: () => import('@/views/device/Dashboard.vue'),
        meta: { title: '设备Dashboard', roles: [DEV] },
      },
      {
        path: 'devices',
        name: 'DeviceManage',
        component: () => import('@/views/device/DeviceManage.vue'),
        meta: { title: '设备台账', roles: [DEV, ORG, COM] },
      },
      {
        path: 'tags',
        name: 'TagManage',
        component: () => import('@/views/doctor/TagManage.vue'),
        meta: { title: '标签管理', roles: [DOC] },
      },

      // ===== C 负责：医生端医疗服务 =====
      {
        path: 'warnings',
        name: 'WarningList',
        component: () => import('@/views/doctor/WarningList.vue'),
        meta: { title: '预警记录', roles: [DOC, ORG, COM] },
      },
      {
        path: 'warning/:id',
        name: 'WarningDetail',
        component: () => import('@/views/doctor/WarningDetail.vue'),
        meta: { title: '预警详情', roles: [DOC, ORG, COM] },
      },
      {
        path: 'followup',
        name: 'FollowupManage',
        component: () => import('@/views/doctor/FollowupManage.vue'),
        meta: { title: '随访管理', roles: [DOC] },
      },
      {
        path: 'followup/execute/:id',
        name: 'FollowupExecute',
        component: () => import('@/views/doctor/FollowupExecute.vue'),
        meta: { title: '执行随访', roles: [DOC] },
      },
      {
        path: 'assessment',
        name: 'AssessmentManage',
        component: () => import('@/views/doctor/AssessmentManage.vue'),
        meta: { title: '评估报告', roles: [DOC] },
      },
      {
        path: 'assessment/create/:id',
        name: 'AssessmentCreate',
        component: () => import('@/views/doctor/AssessmentCreate.vue'),
        meta: { title: '新建评估', roles: [DOC] },
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

// 路由守卫：未登录 → 登录页；角色不匹配 → 403
router.beforeEach((to, _from, next) => {
  const token = getStorage('Token')
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  // 根路径按角色分流到对应工作台
  if (to.path === '/') {
    const role = getStorage('RoleCode')
    const home = { ORG_ADMIN: '/dashboard', COM_ADMIN: '/dashboard', DOCTOR: '/doctor-dashboard', DEVICE_ADMIN: '/device-dashboard' }
    next(home[role] || '/login')
    return
  }

  // 角色权限校验
  const allowedRoles = to.meta.roles
  if (allowedRoles && allowedRoles.length > 0) {
    const userRole = getStorage('RoleCode')
    if (!allowedRoles.includes(userRole)) {
      next('/forbidden')
      return
    }
  }

  document.title = to.meta.title || '智慧医养管理系统'
  next()
})

export default router
