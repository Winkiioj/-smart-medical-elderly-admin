# Dashboard 修改说明 — B

> 日期：2026-07-17

---

## 一、改动内容

### 1. B 的医生 Dashboard 独立为 `DoctorDashboard.vue`

**旧：** `views/doctor/Dashboard.vue`（文件名与 A 的 `DashboardView.vue` 混淆）

**新：** `views/doctor/DoctorDashboard.vue`（医生工作台，路由 `/doctor-dashboard`）

### 2. 侧边栏新增入口

`LayoutView.vue` 侧边栏新增两个菜单：

```
全局工作台  → /dashboard       (A 负责)
医生工作台  → /doctor-dashboard (B 负责)
```

### 3. 全局 Dashboard 改为接入真实后端

`DashboardView.vue`（`/dashboard`）已从硬编码假数据改为调用 `GET /api/doctor/dashboard?doctorId=3` 获取真实数据。

---

## 二、路由表

| 路径 | 组件 | 说明 | 负责人 |
|------|------|------|:--:|
| `/dashboard` | `DashboardView.vue` | 全局工作台 | A |
| `/doctor-dashboard` | `doctor/DoctorDashboard.vue` | 医生个人工作台 | B |
| `/elderly` | `doctor/ElderlyList.vue` | 老人档案 | B |
| `/elderly-new` | `doctor/ElderlyList.vue` | 本月新增（自动筛选） | B |
| `/health-import` | `doctor/HealthImport.vue` | 健康导入 | B |
| `/health-trend` | `doctor/HealthTrend.vue` | 趋势图 | B |
| `/warnings` | `WarningList.vue` | 预警记录（临时过渡页） | B→C |

---

## 三、注意事项

- 之前在 `views/doctor/` 下的旧 `Dashboard.vue` 已删除，请勿再引用
- B 的医生 Dashboard 接入后端接口 `GET /api/doctor/dashboard`，返回格式不变
- 待 C 完成 `WarningController` 后，`/warnings` 路由可替换为 C 的组件
