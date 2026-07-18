# 跨模块接口约定 — B ↔ C

> 创建日期：2026-07-17
> B 负责：elderly, family_contact, health_record
> C 负责：warning_rule, warning_record, followup_plan, followup_record, assessment_*, device, device_monitor_log
> 铁律：只调对方的 Service，绝不直接调 Mapper

---

## 一、B 需要提供给 C 的接口

### 1.1 ElderlyService.getById

```java
Elderly getById(Long id);
```

| 用途 | 所在用例 |
|------|------|
| 预警详情展示老人姓名/性别/年龄/既往病史/紧急联系人 | UC-DOC-05 |
| 随访创建时下拉选择老人、随访执行时展示老人信息卡片 | UC-DOC-06 |
| 评估报告创建时自动拉取老人基本信息 | UC-DOC-07 |

**返回对象 C 需要用到的字段：**

```
id, name, gender(1男2女), birthDate, age, phone, address,
community, doctorId(签约医生), emergencyContact(紧急联系人),
emergencyPhone(紧急联系人电话), medicalHistory(既往病史)
```

---

### 1.2 ElderlyService.listByDoctorId

```java
List<Elderly> listByDoctorId(Long doctorId);
```

| 用途 | 所在用例 |
|------|------|
| 随访创建时下拉搜索框列出该医生的签约老人 | UC-DOC-06 |
| 预警列表按医生ID过滤 | UC-DOC-05 |

**返回对象 C 至少需要：** `id, name, gender, age`

---

### 1.3 HealthRecordService.save

```java
int save(HealthRecord record);
```

| 用途 | 所在用例 |
|------|------|
| 随访执行完成时，将采集的血压/血糖/心率同步写入健康记录表 | UC-DOC-06 |

**C 会传入的字段：**

```
elderlyId, measureDate, systolicPressure(收缩压), diastolicPressure(舒张压),
heartRate(心率), fastingGlucose(空腹血糖), weight(体重), temperature(体温)
```

**返回值：** 成功写入的记录数（1 = 成功）

**说明：** C 写入健康数据后会自行调用 `WarningRuleService.getEnabledRules()` 触发预警规则扫描。

---

### 1.4 HealthRecordService.getTrend

```java
List<Map<String, Object>> getTrend(Long elderlyId, String metricType,
                                   String startDate, String endDate);
```

| 用途 | 所在用例 |
|------|------|
| 预警详情页近7天指标趋势折线图 | UC-DOC-05 |
| 评估报告自动拉取近30天健康数据汇总 | UC-DOC-07 |

**参数说明：**

| 参数 | 说明 |
|------|------|
| `elderlyId` | 老人ID |
| `metricType` | 指标类型：`systolic_pressure` / `diastolic_pressure` / `heart_rate` / `fasting_glucose` / `blood_oxygen` / `weight` / `bmi` |
| `startDate` | 起始日期，格式 `yyyy-MM-dd` |
| `endDate` | 结束日期，格式 `yyyy-MM-dd` |

**返回格式：**

```json
[
  {"date": "2026-07-10", "value": 138},
  {"date": "2026-07-11", "value": 142},
  {"date": "2026-07-12", "value": 135}
]
```

**规则：** 同一天多条记录取最新那条；按日期升序排列。

---

## 二、C 提供给 B 的接口（C 需要写，B 可以用）

### 2.1 WarningRuleService.getEnabledRules

```java
List<WarningRule> getEnabledRules();
```

| 用途 | 所在用例 |
|------|------|
| B 在健康数据导入后扫描所有启用规则，超阈值生成预警 | UC-DOC-03 |

**返回对象字段：**

```
id, ruleName, indicatorType, indicatorName, minValue, maxValue, alertLevel(1轻度/2中度/3重度)
```

---

### 2.2 WarningRecordService.countPendingByDoctor

```java
int countPendingByDoctor(Long doctorId);
```

| 用途 | 所在用例 |
|------|------|
| B 的 Dashboard 待处理预警数（数字卡片） | UC-DOC-01 |

**说明：** `status = 0`（待处理）的记录数。

---

### 2.3 FollowupPlanService.countCompletedByDoctor

```java
int countCompletedByDoctor(Long doctorId);
```

| 用途 | 所在用例 |
|------|------|
| B 的 Dashboard 随访完成率统计 | UC-DOC-01 |

**说明：** `status = 2`（已完成）的记录数。

---

## 三、开发顺序建议

```
B 先完成（第1天）：
  ✅ ElderlyService.getById(id)
  ✅ ElderlyService.listByDoctorId(doctorId)
      → C 拿到后可以开始 UC-DOC-05 预警处理

B 继续完成（第2天）：
  ✅ HealthRecordService.save(record)
  ✅ HealthRecordService.getTrend(...)
      → C 拿到后可以开始 UC-DOC-06 随访执行（同步写健康数据）
      → C 拿到后可以开始 UC-DOC-07 评估报告（健康数据汇总）

C 同步完成（无需等B）：
  ✅ WarningRuleService.getEnabledRules()
  ✅ WarningRecordService.countPendingByDoctor()
  ✅ FollowupPlanService.countCompletedByDoctor()
      → B 拿到后可以 Dashboard 统计数据
```

---

> 西南交通大学 计算机与人工智能学院 软件工程专业 华迪实训
