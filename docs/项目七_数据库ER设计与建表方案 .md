# 智慧医养大数据公共服务平台 — 数据库 ER 设计与建表方案

> **选题：** 项目 #7（医疗板块 · 云端后台管理系统）  
> **数据库：** MySQL 8.0 | **ORM：** MyBatisPlus  
> **设计阶段：** 概念模型（E-R）→ 逻辑模型（表结构）→ 物理模型（DDL）  
> **创建日期：** 2026-07-15

---

## 一、实体识别（从业务描述中提取）

先回顾核心业务流程，从中提取名词作为候选实体：

```
老人入住建档 → 日常健康数据录入 → 自动预警异常 → 医生处理 → 随访跟踪 → 生成评估报告
     │                │                │            │           │            │
  老人/家属        健康记录         预警规则       系统用户    随访计划     评估模板
                                  预警记录                   随访记录     评估报告
```

### 实体清单（21 张表）

```
┌─────────────────────────────────────────────────────────────┐
│                      按业务域分组                             │
├────────────────┬────────────────┬──────────────┬────────────┤
│  用户与权限(5)  │  老人档案(2)    │  健康预警(2)  │  设备管理(2)│
│  sys_user      │  elderly       │  warning_rule │  device    │
│  sys_role      │  family_member │  warning_record│ device_   │
│  sys_menu      │                │               │  monitor   │
│  sys_user_role │                │               │  _log      │
│  sys_role_menu │                │               │            │
├────────────────┼────────────────┼──────────────┼────────────┤
│  评估报告(4)    │  随访管理(2)    │  标签管理(2)  │  消息通知(1)│
│  assessment_   │  followup_plan │  elderly_tag  │  notifi-   │
│  template      │  followup_     │  elderly_tag_ │  cation    │
│  assessment_   │  record        │  mapping      │            │
│  dimension     │                │               │            │
│  assessment_   │                │               │            │
│  report        │                │               │            │
│  assessment_   │                │               │            │
│  score         │                │               │            │
├────────────────┴────────────────┴──────────────┴────────────┤
│  健康数据(1)                                                  │
│  health_record                                               │
└─────────────────────────────────────────────────────────────┘

合计：21张表，覆盖 18 个功能模块
（不含老人端，移除 elderly_account 表）
```

---

## 二、E-R 图（概念模型）

### 2.1 全局 E-R 关系总览

```
                    ┌──────────────┐
                    │   sys_menu   │  (菜单/权限)
                    └──────┬───────┘
                           │ N
                    ┌──────┴───────┐
                    │sys_role_menu │  N:M关联
                    └──────┬───────┘
                           │ M
                    ┌──────┴───────┐
                    │   sys_role   │  (角色)
                    └──────┬───────┘
                           │ M
                    ┌──────┴───────┐
                    │sys_user_role │  N:M关联
                    └──────┬───────┘
                           │ N
                    ┌──────┴───────┐        ┌──────────────┐
                    │   sys_user   │───────→│ notification │
                    └──────┬───────┘ 1    N └──────────────┘
                           │ 1
          ┌────────────────┼─────────────────┐
          │ 1              │ 1                │ 1
          ▼                ▼                  ▼
   ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
   │   elderly    │ │warning_record│ │  followup_   │
   │  (老人档案)   │ │  (预警记录)   │ │   record     │
   └──────┬───────┘ └──────┬───────┘ └──────────────┘
          │ 1              │ N
   ┌──────┼─────────┼──────────┬──────────┐
   │ 1    │ N       │ 1        │ 1        │ 1
   ▼      ▼         ▼          ▼          ▼
family  health    warning   followup  assessment
_member _record   _rule     _plan     _report
                      │                       │
                      │ N                     │ N
                      └───────────┬───────────┘
                                  │ (健康数据触发预警)
                                  ▼
                           warning_record


   ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
   │  assessment_ │────→│  assessment_ │────→│  assessment_ │
   │  template    │ 1  N│  dimension   │ 1  N│  score       │
   └──────┬───────┘     └──────────────┘     └──────────────┘
          │ 1
          │ N
   ┌──────┴───────┐
   │  assessment_ │
   │  report      │
   └──────────────┘


   ┌──────────────┐     ┌──────────────┐
   │ elderly_tag  │←───→│ elderly_tag_ │
   └──────────────┘ M:N │   mapping    │
                        └──────┬───────┘
                               │
                               ▼
                        ┌──────────────┐
                        │   elderly    │
                        └──────────────┘


   ┌──────────────┐     ┌──────────────┐
   │   device     │────→│   device_    │
   └──────────────┘ 1  N│ monitor_log  │
                        └──────────────┘


   ┌──────────────┐     ┌──────────────┐
   │  followup_   │────→│  followup_   │
   │  plan        │ 1  N│  record      │
   └──────────────┘     └──────────────┘
```

### 2.2 实体-关系描述

| 实体 A | 关系 | 实体 B | 说明 |
|--------|:--:|--------|------|
| sys_user | 1 : N | elderly | 一个医生签约多位老人 |
| sys_user | 1 : N | notification | 一个用户有多条消息 |
| sys_user | N : M | sys_role | 用户-角色多对多（通过 sys_user_role） |
| sys_role | N : M | sys_menu | 角色-菜单多对多（通过 sys_role_menu） |
| elderly | 1 : N | family_member | 老人有多个家属 |
| elderly | 1 : N | health_record | 老人有多条健康记录 |
| elderly | 1 : N | warning_record | 老人可能触发多次预警 |
| elderly | 1 : N | assessment_report | 老人有多份评估报告 |
| elderly | 1 : N | followup_plan | 老人有多个随访计划 |
| elderly | N : M | elderly_tag | 老人-标签多对多（通过 elderly_tag_mapping） |
| warning_rule | 1 : N | warning_record | 一条规则触发多条预警 |
| health_record | 1 : N | warning_record | 一次健康数据可能触发预警 |
| assessment_template | 1 : N | assessment_dimension | 模板包含多个评估维度 |
| assessment_template | 1 : N | assessment_report | 一个模板用于多份报告 |
| assessment_report | 1 : N | assessment_score | 一份报告有多条评分 |
| device | 1 : N | device_monitor_log | 一个设备有多条监控日志 |
| followup_plan | 1 : N | followup_record | 一个随访计划有多条执行记录 |

---

## 三、表结构详细设计（逻辑模型）

### 3.1 用户与权限域（5 张表）

#### 表 1：`sys_user` — 系统用户表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 登录用户名 |
| password | VARCHAR(255) | NOT NULL | BCrypt 加密密码 |
| real_name | VARCHAR(50) | NOT NULL | 真实姓名 |
| phone | VARCHAR(20) | | 手机号 |
| email | VARCHAR(100) | | 邮箱 |
| gender | TINYINT | DEFAULT 0 | 0未知 1男 2女 |
| avatar | VARCHAR(255) | | 头像URL |
| status | TINYINT | DEFAULT 1 | 0禁用 1启用 |
| last_login_time | DATETIME | | 最后登录时间 |
| create_time | DATETIME | DEFAULT CURRENT_TIMESTAMP | |
| update_time | DATETIME | ON UPDATE CURRENT_TIMESTAMP | |
| is_deleted | TINYINT | DEFAULT 0 | 0正常 1已删除 |

#### 表 2：`sys_role` — 角色表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| role_code | VARCHAR(50) | NOT NULL, UNIQUE | ROLE_ADMIN, ROLE_DOCTOR, ROLE_DEVICE, ROLE_ORG |
| role_name | VARCHAR(50) | NOT NULL | 系统管理员/社区医生/设备管理员/机构管理员 |
| description | VARCHAR(255) | | |
| status | TINYINT | DEFAULT 1 | 0禁用 1启用 |
| create_time | DATETIME | | |
| update_time | DATETIME | | |

#### 表 3：`sys_menu` — 菜单权限表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| parent_id | BIGINT | DEFAULT 0 | 父菜单ID，0=顶级 |
| menu_name | VARCHAR(50) | NOT NULL | 菜单名称 |
| path | VARCHAR(200) | | 前端路由路径 |
| component | VARCHAR(200) | | 前端组件路径 |
| perms | VARCHAR(100) | | 权限标识符 (如 elderly:delete) |
| icon | VARCHAR(50) | | 图标 |
| menu_type | TINYINT | | 0目录 1菜单 2按钮 |
| sort_order | INT | DEFAULT 0 | 排序 |
| status | TINYINT | DEFAULT 1 | |
| create_time | DATETIME | | |

**示例菜单数据：**

```
系统管理 (目录)
├── 用户管理 (菜单)
│   ├── 查询用户 (按钮: sys:user:list)
│   ├── 新增用户 (按钮: sys:user:add)
│   └── 删除用户 (按钮: sys:user:delete)
├── 角色管理 (菜单)
│   ├── 查询角色 (按钮: sys:role:list)
│   └── 分配权限 (按钮: sys:role:assign)
老人管理 (目录)
├── 老人档案 (菜单)
│   ├── 查询 (按钮: elderly:list)
│   ├── 新增 (按钮: elderly:add)
│   ├── 编辑 (按钮: elderly:edit)
│   └── 删除 (按钮: elderly:delete)
├── 家属信息 (菜单)
└── 老人账户 (菜单)
健康管理 (目录)
├── 健康史 (菜单)
├── 预警规则 (菜单)
├── 预警记录 (菜单)
├── 评估模板 (菜单)
└── 评估报告 (菜单)
设备管理 (目录)
├── 设备台账 (菜单)
└── 设备监控 (菜单)
随访管理 (目录)
├── 重点人群 (菜单)
├── 随访计划 (菜单)
└── 随访记录 (菜单)
报表统计 (目录)
├── 基础报表 (菜单)
└── 报表导出 (菜单)
首页 (菜单)
├── Dashboard (菜单: dashboard)
└── 数据大屏 (菜单: screen)
```

#### 表 4：`sys_user_role` — 用户角色关联表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | BIGINT | NOT NULL, FK → sys_user.id | |
| role_id | BIGINT | NOT NULL, FK → sys_role.id | |
| create_time | DATETIME | | |

> UNIQUE(user_id, role_id)

#### 表 5：`sys_role_menu` — 角色菜单关联表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| role_id | BIGINT | NOT NULL, FK → sys_role.id | |
| menu_id | BIGINT | NOT NULL, FK → sys_menu.id | |
| create_time | DATETIME | | |

> UNIQUE(role_id, menu_id)

**RBAC 五表关系：**

```
sys_user ──→ sys_user_role ←── sys_role ──→ sys_role_menu ←── sys_menu
(用户)      (用户-角色)        (角色)      (角色-菜单)        (菜单/权限)
```

---

### 3.2 老人档案域（3 张表）

#### 表 6：`elderly` — 老人基本信息表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 老人ID |
| name | VARCHAR(50) | NOT NULL | 姓名 |
| id_card | VARCHAR(18) | NOT NULL, UNIQUE | 身份证号 |
| gender | TINYINT | NOT NULL | 1男 2女 |
| birth_date | DATE | NOT NULL | 出生日期（身份证自动提取） |
| age | INT | | 年龄（出生日期计算） |
| phone | VARCHAR(20) | | 联系电话 |
| address | VARCHAR(255) | | 居住地址 |
| community | VARCHAR(100) | NOT NULL | 所属社区 |
| doctor_id | BIGINT | FK → sys_user.id | 签约医生 |
| admission_date | DATE | NOT NULL | 入档日期 |
| height | DECIMAL(5,1) | | 身高(cm) |
| emergency_contact | VARCHAR(50) | | 紧急联系人姓名 |
| emergency_phone | VARCHAR(20) | | 紧急联系人电话 |
| medical_history | TEXT | | 既往病史（逗号分隔，如：高血压,糖尿病） |
| remark | VARCHAR(500) | | 备注 |
| status | TINYINT | DEFAULT 1 | 0离院 1在院 |
| create_by | BIGINT | | 创建人ID |
| create_time | DATETIME | | |
| update_time | DATETIME | | |
| is_deleted | TINYINT | DEFAULT 0 | 0正常 1已删除（软删除） |

> 索引：`idx_name`, `idx_id_card`, `idx_community`, `idx_doctor_id`, `idx_status`

#### 表 7：`family_member` — 家属信息表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| elderly_id | BIGINT | NOT NULL, FK → elderly.id | 关联老人 |
| name | VARCHAR(50) | NOT NULL | 家属姓名 |
| relationship | VARCHAR(20) | NOT NULL | 关系：子女/配偶/兄弟姐妹/其他 |
| phone | VARCHAR(20) | NOT NULL | 联系电话 |
| backup_phone | VARCHAR(20) | | 备用电话 |
| is_emergency | TINYINT | DEFAULT 0 | 0否 1是（紧急联系人） |
| address | VARCHAR(255) | | 家属住址 |
| create_time | DATETIME | | |
| update_time | DATETIME | | |

> 索引：`idx_elderly_id`

---

### 3.3 健康数据域（1 张表）

#### 表 8：`health_record` — 健康数据记录表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| elderly_id | BIGINT | NOT NULL, FK → elderly.id | |
| measure_date | DATE | NOT NULL | 测量日期 |
| measure_time | TIME | | 测量时间 |
| systolic_pressure | INT | | 收缩压(mmHg) |
| diastolic_pressure | INT | | 舒张压(mmHg) |
| heart_rate | INT | | 心率(bpm) |
| blood_sugar | DECIMAL(4,1) | | 血糖(mmol/L) |
| blood_oxygen | INT | | 血氧(%) |
| weight | DECIMAL(5,1) | | 体重(kg) |
| height | DECIMAL(5,1) | | 身高(cm)（记录测量时的身高） |
| bmi | DECIMAL(4,1) | | BMI（自动计算：体重/身高²） |
| temperature | DECIMAL(3,1) | | 体温(℃) |
| remark | VARCHAR(500) | | 备注 |
| record_type | TINYINT | DEFAULT 1 | 1日常检测 2体检 |
| attachment_url | VARCHAR(500) | | 体检报告附件(图片/PDF) |
| create_by | BIGINT | FK → sys_user.id | 录入人 |
| create_time | DATETIME | | |

> 索引：`idx_elderly_measure` (elderly_id, measure_date)

---

### 3.4 健康预警域（2 张表）

#### 表 9：`warning_rule` — 预警规则表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| rule_name | VARCHAR(100) | NOT NULL | 规则名称（如：收缩压重度预警） |
| indicator_type | VARCHAR(30) | NOT NULL | 指标类型：systolic_pressure / diastolic_pressure / heart_rate / blood_sugar / blood_oxygen / bmi / temperature |
| indicator_name | VARCHAR(30) | NOT NULL | 指标中文名（收缩压/血糖...） |
| min_value | DECIMAL(8,2) | | 阈值下限（低于此值触发） |
| max_value | DECIMAL(8,2) | | 阈值上限（高于此值触发） |
| alert_level | TINYINT | NOT NULL | 预警级别：1轻度 2中度 3重度 |
| scope_type | TINYINT | DEFAULT 1 | 适用范围：1全部 2指定社区 3指定标签 |
| scope_value | VARCHAR(500) | | 范围值（社区名或标签ID，逗号分隔） |
| is_enabled | TINYINT | DEFAULT 1 | 0停用 1启用 |
| create_by | BIGINT | | |
| create_time | DATETIME | | |
| update_time | DATETIME | | |

**示例数据：**

```
| rule_name         | indicator_type     | min | max  | level |
| 收缩压轻度预警     | systolic_pressure  | -   | 140  | 1     |
| 收缩压中度预警     | systolic_pressure  | -   | 160  | 2     |
| 收缩压重度预警     | systolic_pressure  | -   | 180  | 3     |
| 舒张压轻度预警     | diastolic_pressure | -   | 90   | 1     |
| 空腹血糖偏高       | blood_sugar        | -   | 6.1  | 1     |
| 空腹血糖明显偏高   | blood_sugar        | -   | 7.0  | 2     |
| 心率过缓           | heart_rate         | 50  | -    | 2     |
| 心率过速           | heart_rate         | -   | 100  | 2     |
| 血氧偏低           | blood_oxygen       | 90  | -    | 3     |
| BMI超重            | bmi                | -   | 28   | 1     |
```

#### 表 10：`warning_record` — 预警记录表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| elderly_id | BIGINT | NOT NULL, FK → elderly.id | |
| rule_id | BIGINT | FK → warning_rule.id | 触发的规则 |
| health_record_id | BIGINT | FK → health_record.id | 触发预警的健康数据 |
| alert_type | VARCHAR(30) | NOT NULL | 预警类型（同 indicator_type） |
| alert_level | TINYINT | NOT NULL | 预警级别：1轻度 2中度 3重度 |
| alert_title | VARCHAR(200) | NOT NULL | 预警标题（如：张奶奶-收缩压重度超标） |
| trigger_value | VARCHAR(50) | NOT NULL | 触发时的指标值（如：178mmHg） |
| threshold_value | VARCHAR(50) | | 阈值（如：>160mmHg） |
| status | TINYINT | DEFAULT 0 | 0待处理 1处理中 2已完成 3已关闭 |
| handler_id | BIGINT | FK → sys_user.id | 处理人 |
| handle_opinion | TEXT | | 处理意见 |
| handle_result | VARCHAR(500) | | 处理结果 |
| handle_time | DATETIME | | 处理时间 |
| create_time | DATETIME | | 触发时间 |
| update_time | DATETIME | | |

> 索引：`idx_elderly_rule` (elderly_id, rule_id), `idx_status`, `idx_create_time`

**状态流转：**

```
待处理(0) ──→ 处理中(1) ──→ 已完成(2)
    │                          │
    └──────── 已关闭(3) ←──────┘
```

**去重规则：** 同一 elderly_id + 同一 alert_type + 24小时内 → 不重复生成预警

---

### 3.5 评估报告域（4 张表）

#### 表 11：`assessment_template` — 评估模板表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| template_name | VARCHAR(100) | NOT NULL | 模板名称（如：老年综合健康评估一级） |
| description | VARCHAR(500) | | 模板说明 |
| dimension_count | INT | DEFAULT 0 | 评估维度数 |
| full_score | INT | DEFAULT 0 | 总分（各维度满分之和） |
| status | TINYINT | DEFAULT 1 | 0停用 1启用 |
| create_by | BIGINT | | |
| create_time | DATETIME | | |
| update_time | DATETIME | | |

#### 表 12：`assessment_dimension` — 评估维度表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| template_id | BIGINT | NOT NULL, FK → assessment_template.id | |
| dimension_name | VARCHAR(100) | NOT NULL | 维度名（日常生活能力、认知功能...） |
| max_score | INT | NOT NULL | 该维度满分 |
| weight | DECIMAL(3,2) | DEFAULT 1.00 | 权重 |
| scoring_guide | TEXT | | 评分标准说明(JSON) |
| sort_order | INT | DEFAULT 0 | 排序 |
| create_time | DATETIME | | |

**scoring_guide JSON 示例：**

```json
{
  "1": "完全依赖他人，无法自理",
  "2": "大部分依赖，仅能完成简单动作",
  "3": "部分依赖，基本生活需协助",
  "4": "基本自理，偶尔需要帮助",
  "5": "完全自理，生活独立"
}
```

#### 表 13：`assessment_report` — 评估报告表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| elderly_id | BIGINT | NOT NULL, FK → elderly.id | |
| template_id | BIGINT | NOT NULL, FK → assessment_template.id | |
| doctor_id | BIGINT | NOT NULL, FK → sys_user.id | 评估医生 |
| total_score | DECIMAL(5,1) | | 总评分 |
| full_score | INT | | 满分 |
| score_level | VARCHAR(20) | | 等级：优秀/良好/一般/较差 |
| suggestion | TEXT | | 综合建议 |
| status | TINYINT | DEFAULT 0 | 0草稿 1正式发布 |
| pdf_url | VARCHAR(500) | | PDF文件路径 |
| create_time | DATETIME | | |
| update_time | DATETIME | | |

> 索引：`idx_elderly_template` (elderly_id, template_id)

#### 表 14：`assessment_score` — 评估打分明细表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| report_id | BIGINT | NOT NULL, FK → assessment_report.id | |
| dimension_id | BIGINT | NOT NULL, FK → assessment_dimension.id | |
| dimension_name | VARCHAR(100) | NOT NULL | 维度名（冗余，方便查询） |
| score | INT | NOT NULL | 实际得分 |
| max_score | INT | NOT NULL | 该维度满分 |
| comment | VARCHAR(500) | | 评语 |

**评估报告四表关系：**

```
assessment_template ──→ assessment_dimension (1:N，模板包含多个维度)
        │
        ├──→ assessment_report (1:N，一个模板用于多份报告)
                │
                └──→ assessment_score (1:N，一份报告有多条评分)
```

---

### 3.6 设备管理域（2 张表）

#### 表 15：`device` — 设备台账表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| device_no | VARCHAR(50) | NOT NULL, UNIQUE | 设备编号（如：DEV-2026-001） |
| device_name | VARCHAR(100) | NOT NULL | 设备名称 |
| device_type | VARCHAR(30) | NOT NULL | 设备类型：血压计/血糖仪/血氧仪/智能腕表/体温计/其他 |
| brand | VARCHAR(50) | | 品牌 |
| model | VARCHAR(50) | | 型号 |
| community | VARCHAR(100) | | 所属社区 |
| elderly_id | BIGINT | FK → elderly.id | 分配给哪位老人（可空） |
| status | TINYINT | DEFAULT 1 | 1在线 2离线 3维修中 4已报废 |
| buy_date | DATE | | 购置日期 |
| warranty_end | DATE | | 保修截止日期 |
| last_report_time | DATETIME | | 最后上报时间（判离线用） |
| remark | VARCHAR(500) | | 备注 |
| create_by | BIGINT | | |
| create_time | DATETIME | | |
| update_time | DATETIME | | |

> 索引：`idx_device_no`, `idx_type`, `idx_status`, `idx_community`

#### 表 16：`device_monitor_log` — 设备监控日志表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| device_id | BIGINT | NOT NULL, FK → device.id | |
| event_type | TINYINT | NOT NULL | 事件类型：1上线 2离线 3数据上报 4状态变更 |
| old_status | TINYINT | | 变更前状态 |
| new_status | TINYINT | | 变更后状态 |
| message | VARCHAR(500) | | 事件描述 |
| create_time | DATETIME | | 事件时间 |

> 索引：`idx_device_time` (device_id, create_time)

---

### 3.7 标签与随访域（5 张表）

#### 表 17：`elderly_tag` — 老人标签表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| tag_name | VARCHAR(30) | NOT NULL, UNIQUE | 标签名（独居、失能、慢性病、高龄、贫困） |
| color | VARCHAR(20) | DEFAULT '#409EFF' | 标签颜色（Hex值） |
| description | VARCHAR(200) | | 标签说明 |
| create_time | DATETIME | | |

**示例数据：**

```
| tag_name | color    | description              |
| 独居     | #E6A23C  | 独自居住，无家人同住       |
| 失能     | #F56C6C  | 丧失部分或全部自理能力     |
| 慢性病   | #909399  | 患有需要长期管理的慢性病   |
| 高龄     | #409EFF  | 80岁以上老人              |
| 贫困     | #67C23A  | 经济困难，需重点关注       |
```

#### 表 18：`elderly_tag_mapping` — 老人标签关联表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| elderly_id | BIGINT | NOT NULL, FK → elderly.id | |
| tag_id | BIGINT | NOT NULL, FK → elderly_tag.id | |
| create_time | DATETIME | | |

> UNIQUE(elderly_id, tag_id)

#### 表 19：`followup_plan` — 随访计划表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| elderly_id | BIGINT | NOT NULL, FK → elderly.id | |
| plan_date | DATE | NOT NULL | 计划随访日期 |
| followup_type | TINYINT | NOT NULL | 随访类型：1电话 2上门 3门诊 |
| followup_content | VARCHAR(500) | | 随访内容/目的 |
| doctor_id | BIGINT | FK → sys_user.id | 执行医生 |
| status | TINYINT | DEFAULT 0 | 0待随访 1已完成 2已逾期 |
| next_plan_date | DATE | | 建议下次随访日期 |
| create_time | DATETIME | | |
| update_time | DATETIME | | |

> 索引：`idx_elderly_plan_date` (elderly_id, plan_date), `idx_status_plan_date` (status, plan_date)

#### 表 20：`followup_record` — 随访记录表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| plan_id | BIGINT | NOT NULL, FK → followup_plan.id | |
| elderly_id | BIGINT | NOT NULL, FK → elderly.id | 冗余，方便查询 |
| followup_date | DATE | NOT NULL | 实际随访日期 |
| followup_type | TINYINT | NOT NULL | 随访方式 |
| elderly_status | VARCHAR(500) | | 老人当前状态描述 |
| intervention | TEXT | | 干预措施/随访内容记录 |
| result | VARCHAR(500) | | 随访结果 |
| next_plan_date | DATE | | 下次随访建议日期 |
| doctor_id | BIGINT | FK → sys_user.id | 执行医生 |
| attachment_url | VARCHAR(500) | | 附件（拍照等） |
| create_time | DATETIME | | |

**随访业务流程：**

```
标记为重点人群的老人
    │
    ▼
自动/手动创建 followup_plan (status=待随访)
    │
    ▼
逾期未完成 → @Scheduled 自动标记为"已逾期"
    │
    ▼
医生执行随访 → 填写 followup_record
    │
    ▼
plan.status → "已完成"
    │
    ▼
自动创建下一次随访计划（next_plan_date → 新 followup_plan）
```

---

### 3.8 消息通知域（1 张表）

#### 表 21：`notification` — 消息通知表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | BIGINT | NOT NULL, FK → sys_user.id | 接收人 |
| title | VARCHAR(200) | NOT NULL | 消息标题 |
| content | TEXT | | 消息内容 |
| type | TINYINT | NOT NULL | 消息类型：1预警通知 2随访提醒 3系统通知 4建档通知 |
| related_id | BIGINT | | 关联业务ID（如预警记录ID） |
| related_type | VARCHAR(30) | | 关联业务类型（warning_record/followup_plan...） |
| is_read | TINYINT | DEFAULT 0 | 0未读 1已读 |
| read_time | DATETIME | | 阅读时间 |
| create_time | DATETIME | | |

> 索引：`idx_user_read` (user_id, is_read, create_time)

---

## 四、表关系速查矩阵

```
                         sys_menu
                            ↑ N:M (sys_role_menu)
                         sys_role
                            ↑ N:M (sys_user_role)
                         sys_user ─────────────────────────────┐
                            │ 1:N                               │ 1:N
              ┌─────────────┼──────────────┬──────────┐        │
              │ 1:N         │ 1:N          │ 1:N      │ 1:N    │ 1:N
              ▼             ▼              ▼          ▼        ▼
           elderly    warning_record  assessment_ followup_ notification
              │             ↑           report     record
     ┌────────┼───────┐     │              ↑          ↑
     │ 1:N    │ 1:1   │ 1:N │              │ 1:N      │ 1:1
     ▼        ▼       ▼     │              │          │
  family  elderly  health──┘      assessment_   followup_
  _member _account _record        score         plan
              │
              │ N:M (elderly_tag_mapping)
              ▼
         elderly_tag


   assessment_template ──→ assessment_dimension
          │ 1:N
          ▼
   assessment_report ──→ assessment_score


   device ──→ device_monitor_log


   warning_rule ──→ warning_record
```

---

## 五、索引设计

| 表 | 索引名 | 字段 | 类型 | 用途 |
|------|------|------|------|------|
| sys_user | uk_username | username | UNIQUE | 登录唯一性 |
| sys_user | idx_real_name | real_name | NORMAL | 姓名搜索 |
| sys_role | uk_role_code | role_code | UNIQUE | 角色编码唯一 |
| elderly | uk_id_card | id_card | UNIQUE | 身份证唯一 |
| elderly | idx_name | name | NORMAL | 姓名搜索 |
| elderly | idx_community | community | NORMAL | 按社区筛选 |
| elderly | idx_doctor_id | doctor_id | NORMAL | 按医生筛选 |
| elderly | idx_status | status | NORMAL | 按状态筛选 |
| family_member | idx_elderly_id | elderly_id | NORMAL | 查某老人的家属 |
| elderly_account | uk_elderly_id | elderly_id | UNIQUE | 一对一关联 |
| health_record | idx_elderly_measure | elderly_id, measure_date | NORMAL | 查某老人某时间段的健康数据 |
| warning_rule | idx_enabled | is_enabled | NORMAL | 只扫描启用的规则 |
| warning_record | idx_elderly_rule | elderly_id, rule_id | NORMAL | 去重判断 |
| warning_record | idx_status | status | NORMAL | 按状态查询 |
| warning_record | idx_create_time | create_time | NORMAL | 按时间排序 |
| assessment_report | idx_elderly_template | elderly_id, template_id | NORMAL | 查某老人的报告 |
| device | uk_device_no | device_no | UNIQUE | 设备编号唯一 |
| device | idx_type | device_type | NORMAL | 按类型筛选 |
| device | idx_status | device_status | NORMAL | 按状态筛选 |
| device_monitor_log | idx_device_time | device_id, create_time | NORMAL | 查某设备的日志 |
| elderly_tag_mapping | uk_elderly_tag | elderly_id, tag_id | UNIQUE | 去重 |
| followup_plan | idx_elderly_plan_date | elderly_id, plan_date | NORMAL | 查某老人的随访计划 |
| followup_plan | idx_status_plan_date | status, plan_date | NORMAL | 定时任务扫描逾期 |
| notification | idx_user_read | user_id, is_read, create_time | NORMAL | 查某人未读消息 |

---

## 六、数据量估算（7天演示用）

| 表 | 预估行数 | 说明 |
|------|:--:|------|
| sys_user | 10~20 | 几个管理员 + 几个医生 + 设备管理员 |
| sys_role | 4 | 固定4个角色 |
| sys_menu | 30~50 | 菜单+按钮权限 |
| elderly | 100~500 | 演示数据，用 SQL 批量生成 |
| family_member | 200~1000 | 每位老人 1-3 个家属 |
| health_record | 2000~10000 | 每位老人 20+ 条历史数据 |
| warning_rule | 10~20 | 预设的预警规则 |
| warning_record | 100~500 | 模拟触发 |
| device | 20~50 | 几种类型的设备 |
| device_monitor_log | 200~500 | 模拟日志 |
| assessment_template | 2~5 | 几个模板即可 |
| assessment_report | 50~200 | 模拟评估记录 |
| followup_plan | 100~500 | 模拟随访计划 |
| notification | 200~500 | 模拟消息 |

**总量约 3000~15000 行**，MySQL 完全可以处理，无需分库分表。

---

## 七、建表 SQL（DDL）

> **完整 DDL 脚本将单独提供 `init.sql` 文件，包含：建表语句 + 索引 + 初始数据 INSERT + 外键约束。**

核心建表示例（以 `elderly` 表为例）：

```sql
CREATE TABLE `elderly` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '老人ID',
    `name` VARCHAR(50) NOT NULL COMMENT '姓名',
    `id_card` VARCHAR(18) NOT NULL COMMENT '身份证号',
    `gender` TINYINT NOT NULL COMMENT '性别：1男 2女',
    `birth_date` DATE NOT NULL COMMENT '出生日期',
    `age` INT COMMENT '年龄',
    `phone` VARCHAR(20) COMMENT '联系电话',
    `address` VARCHAR(255) COMMENT '居住地址',
    `community` VARCHAR(100) NOT NULL COMMENT '所属社区',
    `doctor_id` BIGINT COMMENT '签约医生ID',
    `admission_date` DATE NOT NULL COMMENT '入档日期',
    `height` DECIMAL(5,1) COMMENT '身高(cm)',
    `emergency_contact` VARCHAR(50) COMMENT '紧急联系人姓名',
    `emergency_phone` VARCHAR(20) COMMENT '紧急联系人电话',
    `medical_history` TEXT COMMENT '既往病史',
    `remark` VARCHAR(500) COMMENT '备注',
    `status` TINYINT DEFAULT 1 COMMENT '0离院 1在院',
    `create_by` BIGINT COMMENT '创建人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '0正常 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id_card` (`id_card`),
    KEY `idx_name` (`name`),
    KEY `idx_community` (`community`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人基本信息表';
```

---

## 八、实体-模块-负责人对照表

| 表 | 对应模块 | 负责人 |
|------|------|:--:|
| sys_user, sys_role, sys_menu, sys_user_role, sys_role_menu | ⑰ RBAC权限 | A |
| notification | ② 消息通知 | A |
| elderly_account | ⑯ 老人账户 | A |
| elderly | ③ 老人档案 | A |
| family_member | ④ 家属信息 | A |
| health_record | ⑤ 健康史 | B |
| warning_rule | ⑥ 预警规则 | B |
| warning_record | ⑦ 预警记录 | B |
| assessment_template, assessment_dimension | ⑧ 评估模板 | B |
| assessment_report, assessment_score | ⑨ 报告生成 | B |
| elderly_tag, elderly_tag_mapping | ⑩ 重点人群 | C |
| followup_plan, followup_record | ⑪ 随访计划 | C |
| device | ⑭ 设备台账 | C |
| device_monitor_log | ⑮ 设备监控 | C |

> 注：⑫基础报表 + ⑬报表导出 通过 SQL 聚合查询实现，不需要单独建表。①Dashboard + ⑱数据大屏 读取已有表实时统计。

---

## 九、下一步行动

```
□ 团队 Review 本方案：验证实体和关系是否完整
□ 确认后 → 画正式 E-R 图（Draw.io / ProcessOn）
□ E-R 图确认 → PowerDesigner 16.5 建 PDM（物理数据模型）
□ PDM 导出 DDL SQL → 在 MySQL 中执行建表
□ 编写 init_data.sql（初始数据 + 演示数据）
□ 各成员在各自业务域的 Entity 类中对照建表
```

---

> 📅 创建时间：2026-07-15  
> 📋 关联文档：`项目七_需求分析文档.md` | `项目七_需求理解与用户画像.md` | `项目七_开发流程与团队规约.md`  
> 🗄️ 数据库：MySQL 8.0 | 字符集：utf8mb4 | 引擎：InnoDB  
> 🏫 西南交通大学 计算机与人工智能学院 软件工程专业 华迪实训
