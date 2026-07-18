# B 提供给 C 的接口文档

> B 负责域：elderly, family_contact（family_member）, health_record, import_log
> C 负责域：warning_*, followup_*, assessment_*, device*
> 铁律：**只调对方 Service，绝不直接调 Mapper**

---

## 一、接口清单

| # | 接口 | 返回 | C 对应用例 |
|:--:|------|------|------|
| 1 | `ElderlyService.getById(id)` | Elderly | UC-DOC-05 预警详情 / UC-DOC-06 随访 / UC-DOC-07 评估 |
| 2 | `ElderlyService.listByDoctorId(docId)` | List\<Elderly\> | UC-DOC-06 随访创建下拉选老人 |
| 3 | `HealthRecordService.saveRecord(rec)` | int | UC-DOC-06 随访执行后同步写健康数据 |
| 4 | `HealthRecordService.getTrend(...)` | List\<Map\> | UC-DOC-05 预警趋势图 / UC-DOC-07 评估汇总 |

---

## 二、各接口详细说明

### 2.1 ElderlyService.getById(Long id)

```java
// 注入
@Autowired
private ElderlyService elderlyService;

// 调用
Elderly e = elderlyService.getById(elderlyId);
```

**返回字段（C 会用到的）：**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 老人ID |
| name | String | 姓名 |
| gender | Integer | 1=男 2=女 |
| birthDate | LocalDate | 出生日期 |
| age | Integer | 年龄 |
| phone | String | 联系电话 |
| address | String | 住址 |
| community | String | 所属社区 |
| doctorId | Long | 签约医生ID |
| emergencyContact | String | 紧急联系人姓名 |
| emergencyPhone | String | 紧急联系人电话 |
| medicalHistory | String | 既往病史（逗号分隔） |
| status | Integer | 0=离院 1=在院 |

---

### 2.2 ElderlyService.listByDoctorId(Long doctorId)

```java
List<Elderly> list = elderlyService.listByDoctorId(3L);
// 返回该医生全部在院签约老人，按姓名升序
```

**用于：** 随访创建页面下拉搜索框 / 预警列表按医生筛选

**返回字段至少包含：** id, name, gender, age

---

### 2.3 HealthRecordService.saveRecord(HealthRecord record)

```java
HealthRecord hr = new HealthRecord();
hr.setElderlyId(5L);
hr.setMeasureDate(LocalDate.now());
hr.setSystolicPressure(138);
hr.setDiastolicPressure(88);
hr.setHeartRate(76);
hr.setBloodSugar(BigDecimal.valueOf(6.2));
// ... 其他字段按需设置
int result = healthRecordService.saveRecord(hr);
// 返回值: 1 = 成功
```

**说明：**
- BMI 自动计算（如果传了体重+身高）
- C 写入后需自行调用 `WarningRuleService.getEnabledRules()` 触发预警扫描
- 不需要传 `createBy` / `createTime`（Service 自动处理）

---

### 2.4 HealthRecordService.getTrend(...)

```java
List<Map<String, Object>> trend = healthRecordService.getTrend(
    5L,                              // elderlyId
    "systolic_pressure",             // metricType
    "2026-07-10",                    // startDate (yyyy-MM-dd)
    "2026-07-17"                     // endDate
);
```

**metricType 枚举值：**

| 值 | 含义 |
|------|------|
| `systolic_pressure` | 收缩压 |
| `diastolic_pressure` | 舒张压 |
| `heart_rate` | 心率 |
| `blood_sugar` | 血糖 |
| `blood_oxygen` | 血氧 |
| `weight` | 体重 |
| `height` | 身高 |
| `bmi` | BMI |
| `temperature` | 体温 |

**返回格式：**

```json
[
  {"date": "2026-07-10", "value": 138},
  {"date": "2026-07-11", "value": 142},
  {"date": "2026-07-12", "value": 135}
]
```

同天多条记录取最新一条，按日期升序排列。

---

## 三、Service 所在的包路径

```
com.swjtu.smec.service.ElderlyService        → 老人档案
com.swjtu.smec.service.HealthRecordService   → 健康数据
```

**Entity 所在的包路径：**

```
com.swjtu.smec.entity.Elderly
com.swjtu.smec.entity.HealthRecord
```

---

## 四、当前测试数据

远端数据库 `192.168.52.142:3306/smart_elderly_care`

| doctorId | 用户名 | 密码 |
|:--:|------|------|
| 3 | doctor_zhang | 123456 |

| elderly_id | 姓名 | 年龄 | 签约医生 | 健康记录数 |
|:--:|------|:--:|:--:|:--:|
| 1 | 张奶奶 | 91 | doctorId=3 | 7 |
| 2 | 李大爷 | 75 | doctorId=3 | 6 |
| 3 | 王爷爷 | 79 | doctorId=3 | 7 |
| 4 | 赵奶奶 | 68 | doctorId=3 | 6 |
| 5 | 孙爷爷 | 84 | doctorId=3 | 7 |
| 6 | 赵建国 | 76 | doctorId=3 | 5 |

---

> 问题反馈：找你或 A 都可以。接口签名有变化会在群内同步。
