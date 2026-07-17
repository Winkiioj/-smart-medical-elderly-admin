# 成员 C 开发计划

> **负责人：** 成员 C
> **负责领域：** 医疗服务 + 设备管理
> **数据表：** warning_rule, warning_record, assessment_template, assessment_dimension, assessment_report, assessment_score, device, device_monitor_log, elderly_tag, elderly_tag_mapping, followup_plan, followup_record（12 表）
> **用例：** UC-DOC-05, UC-DOC-06, UC-DOC-07, UC-DOC-08, UC-DEV-01, UC-DEV-02, UC-DEV-03（7 用例）
> **数据库：** smart_elderly_care | 初始化脚本：`docs/initv1.1.sql`
> **更新日期：** 2026-07-17（对齐 initv1.1.sql 实际表结构）

---

## 一、开发总览

### 1.1 模块划分与对应表结构

```
成员C 负责范围（12 张表，对应 initv1.1.sql 第四~七部分）
│
├── 预警模块 (Part 4, 2 表)
│   ├── warning_rule            — 预警规则配置
│   └── warning_record          — 预警记录（含处理字段）
│       对应用例: UC-DOC-05 处理预警
│
├── 评估模块 (Part 5, 4 表)
│   ├── assessment_template     — 评估模板（含 dimension_count/full_score）
│   ├── assessment_dimension    — 评估维度（含 weight/scoring_guide/sort_order）
│   ├── assessment_report       — 评估报告（含 score_level/suggestion/pdf_url）
│   └── assessment_score        — 评估打分明细（关联 report_id + dimension_id）
│       对应用例: UC-DOC-07 生成与填写健康评估报告
│
├── 设备模块 (Part 6, 2 表)
│   ├── device                  — 设备台账（status: 1在线/2离线/3维修中/4已报废）
│   └── device_monitor_log      — 设备监控日志（event_type: 1上线/2离线/3数据上报/4状态变更）
│       对应用例: UC-DEV-01 录入编辑设备台账
│                 UC-DEV-02 查看设备在线状态并处理离线告警
│                 UC-DEV-03 更新设备状态(维修与报废)
│                 UC-DOC-08 提交设备报修（复用 device_monitor_log）
│
├── 标签模块 (Part 7, 2 表)
│   ├── elderly_tag             — 老人标签（独居/失能/慢性病/高龄/贫困）
│   └── elderly_tag_mapping      — 老人-标签多对多关联
│       对应用例: 辅助功能，供 UC-DOC-02/06 使用
│
└── 随访模块 (Part 7, 2 表)
    ├── followup_plan           — 随访计划（status: 0待执行/1执行中/2已完成/3已逾期）
    └── followup_record         — 随访记录（含 elderly_status/intervention/result）
        对应用例: UC-DOC-06 制定与执行随访计划
```

### 1.2 依赖关系

```
C 的模块之间依赖:
  UC-DOC-06(随访) → health_record(调B的Service写入) → 触发 UC-DOC-05(预警)
  UC-DOC-05(预警) ← UC-DOC-03(健康数据导入,B负责)
  UC-DOC-07(评估) ← UC-DOC-02(老人档案,B负责), UC-DOC-06(随访数据)
  UC-DOC-08(设备报修) → UC-DEV-03(维修处理)
  UC-DEV-02(设备监控) ← UC-DEV-01(设备台账数据)
  UC-DEV-03(维修报废) ← UC-DOC-08(医生报修) + UC-DEV-02(离线告警触发)

C 对 B 的依赖:
  - ElderlyService.getById(elderlyId) → Elderly 对象
  - ElderlyService.listByDoctorId(doctorId) → List<Elderly>
  - HealthRecordService: 趋势数据查询、健康数据写入
```

### 1.3 开发顺序

```
Phase 1: 后端 Entity → Mapper → Service → Controller
  顺序: 预警 → 设备 → 标签 → 随访 → 评估（按 initv1.1.sql 表顺序）

Phase 2: 前端页面
  顺序: P0 用例优先 → P1 用例

Phase 3: 联调测试
```

---

## 二、后端文件清单

### 2.1 预警模块（warning_rule, warning_record）

#### Entity

| # | 文件 | 关键字段（对齐 initv1.1.sql） |
|---|------|------|
| 1 | `entity/WarningRule.java` | id, ruleName, indicatorType, indicatorName, minValue, maxValue, alertLevel(1轻度/2中度/3重度), scopeType(1全部/2指定社区/3指定标签), scopeValue, isEnabled, createBy, createTime, updateTime |
| 2 | `entity/WarningRecord.java` | id, elderlyId, ruleId, healthRecordId, alertType, alertLevel, alertTitle, triggerValue, thresholdValue, status(0待处理/1处理中/2已完成/3已关闭), handlerId, handleOpinion, handleResult, handleTime, createTime(触发时间), updateTime |

#### Mapper

| # | 文件 | 自定义方法 |
|---|------|------|
| 3 | `mapper/WarningRuleMapper.java` | `selectEnabled()` — 查询 is_enabled=1 的规则（供B的健康数据导入后扫描） |
| 4 | `mapper/WarningRecordMapper.java` | `selectPageByDoctor(Page, doctorId, alertLevel, alertType, status, startTime, endTime)` — 分页查询；`countPendingByDoctor(doctorId)` — 待处理数 |

#### Service

| # | 文件 | 关键方法 |
|---|------|------|
| 5 | `service/WarningRuleService.java` | `getEnabledRules()` — 供B调用 |
| 6 | `service/WarningRecordService.java` | `pageByDoctor(...)` / `getDetail(id)` / `accept(id, doctorId)` / `complete(id, opinion, result)` / `close(id, reason)` / `countPendingByDoctor(doctorId)` |
| 7 | `service/impl/WarningRuleServiceImpl.java` | 标准实现 |
| 8 | `service/impl/WarningRecordServiceImpl.java` | **状态机**: 0→1(接单), 1→2(完成), 0/1→3(关闭)。接单记录 handlerId+handleTime；完成必填 handleOpinion+handleResult；关闭必填 handleOpinion(写关闭原因) |

#### Controller

| # | 文件 | 端点 |
|---|------|------|
| 9 | `controller/WarningController.java` | `GET /api/warning/page` `GET /api/warning/detail/{id}` `POST /api/warning/accept/{id}` `POST /api/warning/complete` `POST /api/warning/close` |

---

### 2.2 设备模块（device, device_monitor_log）

#### Entity

| # | 文件 | 关键字段（对齐 initv1.1.sql） |
|---|------|------|
| 10 | `entity/Device.java` | id, deviceNo, deviceName, deviceType(血压计/血糖仪/血氧仪/智能腕表/体温计/其他), brand, model, community, elderlyId(可空), status(1在线/2离线/3维修中/4已报废), buyDate, warrantyEnd, lastReportTime(判离线用), remark, createBy, createTime, updateTime |
| 11 | `entity/DeviceMonitorLog.java` | id, deviceId, eventType(1上线/2离线/3数据上报/4状态变更), oldStatus, newStatus, message, createTime |

#### Mapper

| # | 文件 | 自定义方法 |
|---|------|------|
| 12 | `mapper/DeviceMapper.java` | `selectByDeviceNo(deviceNo)` — 编号查重；`selectPage(Page, status, type)` — 分页；`selectByCommunity(community)` — 按社区查；`countByStatus(status)` — 统计 |
| 13 | `mapper/DeviceMonitorLogMapper.java` | `selectByDeviceId(deviceId, limit)` — 最近N条；`selectHeartbeatsByDevice(deviceId, startTime, endTime)` — 心跳历史 |

#### Service

| # | 文件 | 关键方法 |
|---|------|------|
| 14 | `service/DeviceService.java` | `pageList(...)` / `getDetail(id)` / `add(device)` / `updateInfo(device)` / `updateStatus(id, newStatus, reason)` / `handleAlarm(deviceId, handleType, desc)` / `getStats()` |
| 15 | `service/DeviceMonitorLogService.java` | `getRecentLogs(deviceId, limit)` / `logEvent(deviceId, eventType, oldStatus, newStatus, message)` |
| 16 | `service/impl/DeviceServiceImpl.java` | **编号生成**: `DEV-{yyyyMMdd}-{序号3位}`；**乐观锁**: 编辑比较 updateTime；**状态机**: 1/2/3→4(维修中), 1/2/3/4→5(已报废终态, 不存在的状态-实际为4已报废)；所有状态变更调 deviceMonitorLogService.logEvent()；**离线判定**: lastReportTime > 30分钟→离线；**设备报修**: 复用 deviceMonitorLogService 记录 eventType=4 |
| 17 | `service/impl/DeviceMonitorLogServiceImpl.java` | 标准实现 |

#### Controller

| # | 文件 | 端点 |
|---|------|------|
| 18 | `controller/DeviceController.java` | `GET /api/device/page` `GET /api/device/detail/{id}` `POST /api/device/add` `PUT /api/device/update` `PUT /api/device/status/{id}` `POST /api/device/handle-alarm` `GET /api/device/stats` |
| 19 | `controller/RepairController.java` | 医生报修 `POST /api/repair/submit`；管理员 `GET /api/repair/list` `GET /api/repair/detail/{id}` `POST /api/repair/accept/{id}` `POST /api/repair/complete/{id}` `POST /api/repair/reject/{id}`；医生查自己 `GET /api/repair/my-repairs` |

---

### 2.3 标签模块（elderly_tag, elderly_tag_mapping）

#### Entity

| # | 文件 | 关键字段 |
|---|------|------|
| 20 | `entity/ElderlyTag.java` | id, tagName, color(default #409EFF), description, createTime |
| 21 | `entity/ElderlyTagMapping.java` | id, elderlyId, tagId, createTime |

#### Mapper

| # | 文件 | 自定义方法 |
|---|------|------|
| 22 | `mapper/ElderlyTagMapper.java` | `extends BaseMapper<ElderlyTag>` |
| 23 | `mapper/ElderlyTagMappingMapper.java` | `selectByElderlyId(elderlyId)` — 查某老人的所有标签；`deleteByElderlyId(elderlyId)` — 编辑时先删后插 |

#### Service

| # | 文件 | 关键方法 |
|---|------|------|
| 24 | `service/ElderlyTagService.java` | CRUD + `listAll()` |
| 25 | `service/ElderlyTagMappingService.java` | `getTagsByElderly(elderlyId)` / `saveTags(elderlyId, List<tagId>)` — 先删后插 |
| 26-27 | `impl/` 两个实现类 | 标准实现 |

#### Controller

| # | 文件 | 端点 |
|---|------|------|
| 28 | `controller/ElderlyTagController.java` | `GET /api/tag/list` `POST /api/tag` `PUT /api/tag` `DELETE /api/tag/{id}`；`GET /api/tag/elderly/{elderlyId}` `PUT /api/tag/elderly/{elderlyId}` |

---

### 2.4 随访模块（followup_plan, followup_record）

#### Entity

| # | 文件 | 关键字段（对齐 initv1.1.sql） |
|---|------|------|
| 29 | `entity/FollowupPlan.java` | id, elderlyId, planDate, followupType(1电话/2上门/3门诊), followupContent, doctorId, status(0待执行/1执行中/2已完成/3已逾期), nextPlanDate, createTime, updateTime |
| 30 | `entity/FollowupRecord.java` | id, planId, elderlyId, followupDate, followupType, elderlyStatus, intervention, result, nextPlanDate, doctorId, attachmentUrl, createTime |

#### Mapper

| # | 文件 | 自定义方法 |
|---|------|------|
| 31 | `mapper/FollowupPlanMapper.java` | `selectPageByDoctor(Page, doctorId, type, status, startDate, endDate)` / `selectTodayByDoctor(doctorId)` / `selectOverdue()` — 逾期扫描 / `countCompletedByDoctor(doctorId)` |
| 32 | `mapper/FollowupRecordMapper.java` | `selectLatestByElderly(elderlyId)` / `selectRecentByElderly(elderlyId, limit)` |

#### Service

| # | 文件 | 关键方法 |
|---|------|------|
| 33 | `service/FollowupPlanService.java` | `pageByDoctor(...)` / `create(plan)` / `start(planId, doctorId)` / `complete(planId, record)` / `cancelOverdue()` — @Scheduled / `countCompletedByDoctor(doctorId)` |
| 34 | `service/FollowupRecordService.java` | `getLatestByElderly(elderlyId)` / `getRecentByElderly(elderlyId, limit)` |
| 35 | `service/impl/FollowupPlanServiceImpl.java` | **状态机**: 0→1(开始执行), 1→2(完成), 0/1→3(逾期自动取消, @Scheduled 每天凌晨2点)。完成随访时: 若随访中采集了健康指标, 调用B的 HealthRecordService 写入 health_record 并触发预警扫描；自动生成下次计划(nextPlanDate或默认间隔) |
| 36 | `service/impl/FollowupRecordServiceImpl.java` | 标准实现 |

> 注意：需要在 `Application.java` 加 `@EnableScheduling`（通知A）。

#### Controller

| # | 文件 | 端点 |
|---|------|------|
| 37 | `controller/FollowupController.java` | `GET /api/followup/plan/page` `POST /api/followup/plan/create` `POST /api/followup/plan/start/{planId}` `POST /api/followup/plan/complete` `GET /api/followup/plan/today` `GET /api/followup/record/latest/{elderlyId}` `GET /api/followup/record/recent/{elderlyId}` |

---

### 2.5 评估模块（assessment_template, assessment_dimension, assessment_report, assessment_score）

#### Entity

| # | 文件 | 关键字段（对齐 initv1.1.sql） |
|---|------|------|
| 38 | `entity/AssessmentTemplate.java` | id, templateName, description, dimensionCount, fullScore, status(0停用/1启用), createBy, createTime, updateTime |
| 39 | `entity/AssessmentDimension.java` | id, templateId, dimensionName, maxScore, weight(default 1.00), scoringGuide(TEXT, JSON格式评分标准), sortOrder, createTime |
| 40 | `entity/AssessmentReport.java` | id, elderlyId, templateId, doctorId, totalScore, fullScore, scoreLevel(优秀/良好/一般/较差), suggestion, status(0草稿/1正式发布), pdfUrl, createTime, updateTime |
| 41 | `entity/AssessmentScore.java` | id, reportId, dimensionId, dimensionName, score, maxScore, comment |

#### Mapper

| # | 文件 | 自定义方法 |
|---|------|------|
| 42 | `mapper/AssessmentTemplateMapper.java` | `selectEnabled()` |
| 43 | `mapper/AssessmentDimensionMapper.java` | `selectByTemplateId(templateId)` — 按模板查维度列表 |
| 44 | `mapper/AssessmentReportMapper.java` | `selectPageByDoctor(Page, doctorId)` / `selectLatestByElderly(elderlyId, templateId)` — 同老人同模板最新报告(一月去重) |
| 45 | `mapper/AssessmentScoreMapper.java` | `selectByReportId(reportId)` — 查某报告所有维度得分 |

#### Service

| # | 文件 | 关键方法 |
|---|------|------|
| 46 | `service/AssessmentTemplateService.java` | `getEnabled()` / `getTemplateWithDimensions(id)` — 含维度列表 |
| 47-48 | `service/AssessmentDimensionService.java` + impl | 标准 CRUD |
| 49 | `service/AssessmentReportService.java` | `pageList(...)` / `create(elderlyId, templateId)` / `saveScores(reportId, List<AssessmentScore>)` — 暂存 / `complete(reportId, conclusion, healthAdvice)` / `getDetail(id)` |
| 50 | `service/AssessmentScoreService.java` | `getScoresByReport(reportId)` — 查分数明细 |
| 51 | `service/impl/AssessmentReportServiceImpl.java` | **创建**: 自动拉取B的 ElderlyService(老人信息) + HealthRecordService(近30天数据) + C自己的 WarningRecordService(预警汇总) + FollowupRecordService(随访摘要)。**暂存**: 批量保存 assessment_score 记录(先删后插)。**完成**: 校验所有维度已评分，加权计算 totalScore，判定 scoreLevel(≥90%优秀/75-89%良好/60-74%一般/<60%较差)，校验 conclusion 不为空，更新 status=1。**一月去重**: 同老人+同模板本月已有正式报告时阻止创建。**修订版本**: 若已有已完成报告，新创建的报告关联为修订版本。PDF 生成: 前端 window.print() 或后端 iText |

#### Controller

| # | 文件 | 端点 |
|---|------|------|
| 52 | `controller/AssessmentController.java` | `GET /api/assessment/template/list` `GET /api/assessment/template/{id}` — 含维度；`GET /api/assessment/report/page` `POST /api/assessment/report/create` `PUT /api/assessment/report/scores/{reportId}` `POST /api/assessment/report/complete/{reportId}` `GET /api/assessment/report/detail/{id}` |

---

## 三、前端文件清单

### 3.1 API 封装文件

| # | 文件路径 | 封装端点 |
|---|---------|------|
| 53 | `frontend/src/api/warning.js` | getPage / getDetail / accept / complete / close |
| 54 | `frontend/src/api/device.js` | getPage / getDetail / add / update / updateStatus / handleAlarm / getStats |
| 55 | `frontend/src/api/repair.js` | submit / getList / getDetail / accept / complete / reject / getMyRepairs |
| 56 | `frontend/src/api/tag.js` | getList / add / update / delete / getByElderly / saveByElderly |
| 57 | `frontend/src/api/followup.js` | getPlanPage / createPlan / startPlan / completePlan / getTodayPlans / getLatestRecord / getRecentRecords |
| 58 | `frontend/src/api/assessment.js` | getTemplateList / getTemplateWithDimensions / getReportPage / createReport / saveScores / completeReport / getReportDetail |

### 3.2 Vue 页面组件

| # | 文件路径 | 对应用例 | 核心元素 |
|---|---------|------|------|
| 59 | `views/doctor/WarningManage.vue` | UC-DOC-05 | 筛选栏(级别/类型/状态/时间)+预警表格(级别着色)+分页；行点击→WarningDetail |
| 60 | `views/doctor/WarningDetail.vue` | UC-DOC-05 | 四个卡片区：预警基本信息、近7天趋势折线图(ECharts+阈值线)、老人信息卡片(el-descriptions)、处理时间线(el-timeline)；底部操作：接单/完成表单(opinion+result)/关闭弹窗(原因必选) |
| 61 | `views/doctor/FollowupManage.vue` | UC-DOC-06 | 筛选+新建按钮+计划列表(今日高亮/逾期橙标/状态标签)；点击行→FollowupExecute |
| 62 | `views/doctor/FollowupExecute.vue` | UC-DOC-06 | 老人信息卡片+最近3次摘要；表单：健康指标(血压/心率/血糖/体重/体温)+用药+生活方式+心理筛查PHQ-2(自动计分≥3警告)+结论；完成按钮→自动生成下次计划 |
| 63 | `views/doctor/AssessmentManage.vue` | UC-DOC-07 | 报告列表(老人/模板/状态/等级/时间)+新建按钮；点击行→AssessmentCreate或Preview |
| 64 | `views/doctor/AssessmentCreate.vue` | UC-DOC-07 | 步骤1选老人+模板→自动加载基础数据汇总(el-skeleton)；步骤2逐维度评分(el-card+el-radio-group,实时计分,自动暂存)；步骤3预览(ECharts雷达图+明细表+总分等级)+结论(≥20字)+建议；步骤4完成→生成PDF |
| 65 | `views/doctor/DeviceRepair.vue` | UC-DOC-08 | 报修表单(设备下拉/故障类型/描述/时间/紧急程度)+提交；下方我的报修列表(状态+进度) |
| 66 | `views/device/DeviceManage.vue` | UC-DEV-01 | 筛选(状态/类型/搜索)+录入按钮+设备表格(编号/名称/类型/社区/指示灯/购入/质保)；录入编辑el-dialog表单；行→DeviceDetail |
| 67 | `views/device/DeviceDetail.vue` | UC-DEV-01/02 | 基本信息+质保(到期标签)+运行状态(心跳/电量)+心跳时间线(ECharts散点图)+操作日志el-timeline；底部：状态管理按钮/处理告警按钮 |
| 68 | `views/device/DeviceMonitor.vue` | UC-DEV-02 | 顶部4数字卡片(30秒刷新)+饼图(ECharts)+实时列表(绿黄红灰指示灯+心跳+离线时长)；离线红底高亮→处理对话框 |
| 69 | `views/device/DeviceStatus.vue` | UC-DEV-03 | 双Tab：维修请求列表(来自UC-DOC-08报修)+直接状态变更。状态变更对话框：选维修中/已报废，填原因+说明；报废必勾确认复选框 |

### 3.3 路由配置

在 `frontend/src/router/index.js` 中补充 C 的路由（取消注释并关联组件）：

```javascript
{ path: '/warnings', component: () => import('@/views/doctor/WarningManage.vue'), meta: { roles: ['DOC'] } },
{ path: '/warnings/:id', component: () => import('@/views/doctor/WarningDetail.vue'), meta: { roles: ['DOC'] } },
{ path: '/followup', component: () => import('@/views/doctor/FollowupManage.vue'), meta: { roles: ['DOC'] } },
{ path: '/followup/execute/:planId', component: () => import('@/views/doctor/FollowupExecute.vue'), meta: { roles: ['DOC'] } },
{ path: '/assessment', component: () => import('@/views/doctor/AssessmentManage.vue'), meta: { roles: ['DOC'] } },
{ path: '/assessment/create', component: () => import('@/views/doctor/AssessmentCreate.vue'), meta: { roles: ['DOC'] } },
{ path: '/device-repair', component: () => import('@/views/doctor/DeviceRepair.vue'), meta: { roles: ['DOC'] } },
{ path: '/devices', component: () => import('@/views/device/DeviceManage.vue'), meta: { roles: ['DEV'] } },
{ path: '/devices/:id', component: () => import('@/views/device/DeviceDetail.vue'), meta: { roles: ['DEV'] } },
{ path: '/device-monitor', component: () => import('@/views/device/DeviceMonitor.vue'), meta: { roles: ['DEV'] } },
{ path: '/device-status', component: () => import('@/views/device/DeviceStatus.vue'), meta: { roles: ['DEV'] } },
```

### 3.4 整体布局规范（Element Plus）

所有 C 负责页面嵌入统一的 Element Plus 后台管理布局，参考 [Element Plus Container 布局容器](https://element-plus.org/zh-CN/component/container.html)。

```
┌──────────────────────────────────────────────────────────┐
│  🏥 智慧医养管理系统        │  🔔 消息  👤 张医生 ▼    │ ← el-header (60px)
├────────────────────────────┼─────────────────────────────┤
│  ┌──────────────────┐      │                             │
│  │ 📋 预警管理       │      │  <router-view />           │
│  │    └ 预警记录     │      │                             │
│  │ 📋 随访管理       │      │  ┌─────────────────────┐   │
│  │    └ 随访计划     │      │  │  面包屑导航          │   │
│  │ 📋 评估管理       │      │  │  预警记录 > 详情     │   │
│  │    └ 评估报告     │      │  ├─────────────────────┤   │
│  │ ─────────────    │      │  │                     │   │
│  │ 📋 设备管理       │      │  │   主数据区          │   │
│  │    └ 设备台账     │      │  │   (列表/表单/图表)  │   │
│  │    └ 设备监控     │      │  │                     │   │
│  │    └ 设备报修     │      │  │                     │   │
│  └──────────────────┘      │  └─────────────────────┘   │
│  ← el-aside (220px)        │  ← el-main                  │
└──────────────────────────────────────────────────────────┘
```

**Element Plus 组件映射：**

| 区域 | Element Plus 组件 | 说明 |
|------|------|------|
| 整体容器 | `el-container` | 外层 |
| 左侧菜单 | `el-aside` → `el-menu`（router 模式，`collapse` 可折叠） | 宽度 220px，背景深色 `#304156` |
| 菜单项 | `el-menu-item` / `el-sub-menu` | 带图标，`index` 对应路由 path |
| 顶部栏 | `el-header` | 高度 60px，白色背景 + 底部阴影 |
| 用户头像 | `el-avatar` + `el-dropdown` | 右上角，下拉菜单：个人信息 / 修改密码 / 退出登录 |
| 消息通知 | `el-badge` + `el-icon` | 未读数红点角标，点击弹出 `el-popover` 消息列表 |
| 主内容区 | `el-main` | 背景 `#f0f2f5`，内边距 20px |
| 面包屑 | `el-breadcrumb` | 主内容区顶部，自动根据路由生成 |

**各页面内嵌在主内容区的 `<router-view />` 中，页面内部组件：**

| 页面内元素 | Element Plus 组件 |
|------|------|
| 筛选栏 | `el-form :inline="true"` + `el-select` / `el-date-picker` / `el-input` |
| 数据表格 | `el-table`（`stripe` `border`）+ `el-table-column` + `el-pagination` |
| 表单弹窗 | `el-dialog` + `el-form`（`label-width="100px"`）+ `el-form-item` |
| 状态标签 | `el-tag`（`type` 属性控制颜色） |
| 卡片分区 | `el-card`（`shadow="hover"`） |
| 按钮 | `el-button`（`type="primary/success/warning/danger/info"`） |
| 图标 | `@element-plus/icons-vue`（全局已注册，如 `<Edit />` `<Delete />`） |
| 确认弹窗 | `ElMessageBox.confirm()` |
| Toast 提示 | `ElMessage.success/error/warning()` |
| 加载态 | `v-loading` 指令 或 `el-skeleton` |
| 空状态 | `el-empty description="暂无数据"` |
| 时间线 | `el-timeline` + `el-timeline-item` |
| 步骤条 | `el-steps` + `el-step`（评估创建流程用） |
| 拖拽上传 | `el-upload`（`drag` 属性启用拖拽） |

### 3.5 核心页面布局

页面嵌入主布局后，内容区标准结构：

```
[面包屑导航]
[页面标题 <h2>]
[筛选/操作栏: el-form inline + el-button]
[数据区: el-table / el-card / ECharts]
[分页: el-pagination]
```

**状态标签颜色规范（el-tag type）：**

| 业务 | 标签 | type |
|------|------|:--:|
| 预警状态 | 待处理 / 处理中 / 已完成 / 已关闭 | warning / primary / success / info |
| 预警级别 | 轻度 / 中度 / 重度 | info / warning / danger |
| 设备状态 | 在线 / 离线 / 维修中 / 已报废 | success / danger / warning / info |
| 随访状态 | 待执行 / 执行中 / 已完成 / 已逾期 | info / primary / success / danger |
| 报修紧急 | 普通 / 紧急 | info / danger |
| 评估等级 | 优秀 / 良好 / 一般 / 较差 | success / primary / warning / danger |

### 3.6 图表规范（ECharts）

| 页面 | 图表类型 | 配置要点 |
|------|------|------|
| 预警详情 | 折线图(line) | 蓝色折线 + 绿色 normal range `markArea` + 红色超标 `markPoint` |
| 评估结果 | 雷达图(radar) | 各维度百分比，填充半透明 |
| 设备监控 | 饼图(pie) | 在线/离线/异常/维修中 四色分布 |
| 设备详情 | 散点图(scatter) | 心跳时间线，绿色=正常，红色间隙=离线 |

### 3.7 UI 组件库参考

所有组件用法查阅 Element Plus 官方文档：https://element-plus.org/zh-CN/

**交互：** 增删改成功→ElMessage.success；不可逆操作→ElMessageBox.confirm；加载中→v-loading；无数据→el-empty；表单→el-form rules 实时校验

---

## 四、跨模块接口约定（需与 B 确认）

| # | C 需要 B 提供 | 调用场景 |
|---|------|------|
| 1 | `ElderlyService.getById(Long id)` → Elderly | 预警详情/随访选择老人/评估创建/标签关联 |
| 2 | `ElderlyService.listByDoctorId(Long doctorId)` → List\<Elderly\> | 随访创建下拉/预警按医生过滤 |
| 3 | `HealthRecordService.getTrend(Long elderlyId, String indicatorType, String startDate, String endDate)` → List\<{date, value}\> | 预警详情近7天趋势图/评估报告健康汇总 |
| 4 | `HealthRecordService.save(HealthRecord record)` | 随访完成时同步写入健康指标 |

| # | B 需要 C 提供 | 调用场景 |
|---|------|------|
| 1 | `WarningRecordService.countPendingByDoctor(Long doctorId)` → int | B的Dashboard待处理预警数 |
| 2 | `WarningRuleService.getEnabledRules()` → List\<WarningRule\> | B的健康数据导入后触发预警扫描 |
| 3 | `FollowupPlanService.countCompletedByDoctor(Long doctorId)` → int | B的Dashboard随访完成率 |

---

## 五、文件提交清单

| 层 | 文件数 | 内容 |
|------|:--:|------|
| Entity | 12 | WarningRule/Record, AssessmentTemplate/Dimension/Report/Score, Device/MonitorLog, ElderlyTag/Mapping, FollowupPlan/Record |
| Mapper | 12 | 每表一个，含自定义查询方法 |
| Service | 24 | 12接口 + 12实现，含状态机/定时任务/跨模块调用 |
| Controller | 6 | Warning/Device/Repair/ElderlyTag/Followup/Assessment |
| API (frontend) | 6 | warning.js/device.js/repair.js/tag.js/followup.js/assessment.js |
| Views (Vue) | 11 | WarningManage/Detail, FollowupManage/Execute, AssessmentManage/Create, DeviceRepair, DeviceManage/Detail/Monitor/Status |
| Router | 1 | 11条路由配置 |

> **文档版本：** v2.0 | 2026-07-17
> **对齐：** `initv1.1.sql` 实际表结构
> **西南交通大学 计算机与人工智能学院 软件工程专业 华迪实训**
