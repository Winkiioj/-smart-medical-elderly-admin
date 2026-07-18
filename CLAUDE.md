# SMECMS 项目专属编码规范

> 适用于智慧医养大数据公共服务平台（Smart Medical-Elderly Care Management System）
> 在你为本项目写任何代码前，必须阅读并遵循本规范

---

## 一、项目信息

| 项 | 值 |
|------|------|
| 项目名 | 智慧医养大数据公共服务平台 / SMECMS |
| 后端 | Spring Boot 3.5.13 + MyBatis-Plus 3.5.12 + Java 17 |
| 前端 | Vue 3 + Vite + Element Plus + ECharts |
| 数据库 | MySQL（远程 192.168.52.142:3306，库名 smart_elderly_care） |
| 包名 | `com.swjtu.smec` |
| 文件编码 | UTF-8 |
| IDE | IntelliJ IDEA 2025.2.4 |

## 二、后端分层架构

```
controller/     → @RestController, 只做参数接收和调用 Service，返回 CommonResult
service/        → 接口继承 IService<Entity>
service/impl/   → @Service + @Transactional, 继承 ServiceImpl<Mapper, Entity>
mapper/         → @Mapper, 继承 BaseMapper<Entity>
entity/         → @Data + @TableName + @TableId(type=IdType.AUTO)
common/
  config/       → @Configuration (MybatisPlusConfig, WebConfig, SecurityConfig)
  result/       → CommonResult<T>
  exception/    → @RestControllerAdvice 全局异常
  enums/        → GlobalErrorCodeConstants
  annotation/   → @NoToken（开发阶段跳过认证）
aop/            → AroundCut（Token 校验切面）
```

**调用链（单向，不可跨越）：**
```
Controller → Service接口 → ServiceImpl → Mapper → DB
                  ↑ 跨域读取只能调别人的 Service，绝不直接调 Mapper
```

## 三、后端代码模板

### 3.1 Entity
```java
@Data
@TableName("table_name")
public class Xxx {
    @TableId(type = IdType.AUTO)
    private Long id;
    // 字段名用驼峰，对应数据库下划线字段
    private LocalDateTime createTime;
}
```
- 必须用 Lombok `@Data`
- 字段类型：日期用 `LocalDate`/`LocalDateTime`，布尔用 `Integer(0/1)`
- 所有 C 负责的表**没有** `isDeleted` 字段，不用 `@TableLogic`

### 3.2 Mapper
```java
@Mapper
public interface XxxMapper extends BaseMapper<Xxx> {
    // 简单查询用 @Select 注解
    // 复杂动态查询用 resources/mapper/XxxMapper.xml
}
```

### 3.3 Service
```java
// 接口
public interface XxxService extends IService<Xxx> {
    CommonResult pageList(...);  // 分页查询
    CommonResult getDetail(Long id);  // 详情
    CommonResult add(Xxx entity, Long operatorId);  // 新增
    CommonResult update(Xxx entity);  // 编辑
    CommonResult delete(Long id);  // 删除（状态变更）
}

// 实现
@Service
@Transactional
public class XxxServiceImpl extends ServiceImpl<XxxMapper, Xxx> implements XxxService {
    // 所有写操作必须明确记录操作人和日志
    // 跨模块读取：@Autowired 注入别人的 Service（不是 Mapper）
}
```

### 3.4 Controller
```java
@RestController
@RequestMapping("/api/xxx")
public class XxxController {
    @Autowired
    private XxxService xxxService;

    @NoToken  // 开发阶段临时加，等A做好登录后删
    @GetMapping("/page")
    public CommonResult page(@RequestParam(defaultValue = "1") int pageNo, ...) {
        return xxxService.pageList(...);
    }
}
```
- 端点命名：RESTful 风格，`/api/实体名/动作`
- 参数校验在 Controller 做，业务逻辑在 Service 做
- 所有方法返回 `CommonResult`，不 try-catch（全局异常兜底）

### 3.5 CommonResult
```java
// 成功
CommonResult.success(data);
CommonResult.success(data, total);  // 分页时传 total

// 失败
CommonResult.error(400, "错误描述");
CommonResult.error(404, "资源不存在");
```

## 四、三人分工隔离（三大铁律）

| 成员 | 负责表 | 负责用例 |
|------|------|------|
| A | sys_user, sys_role, sys_menu, sys_user_role, sys_role_menu, notification, community | UC-ORG-01~02, UC-COM-01~03 |
| B | elderly, family_contact, health_record, import_log, operation_log | UC-DOC-01~04 |
| C | warning_rule, warning_record, assessment_template, assessment_dimension, assessment_report, assessment_score, device, device_monitor_log, elderly_tag, elderly_tag_mapping, followup_plan, followup_record | UC-DOC-05~08, UC-DEV-01~03 |

### 铁律1：表归属 = 代码归属
> C 的表只有 C 能建 Entity/Mapper/Service。B 的表只有 B 能建。绝不越界。

### 铁律2：跨域读取 = 注入对方 Service
```java
// ✅ C 需要读 B 的 elderly
@Autowired private ElderlyService elderlyService;  // 只调 getById、listByDoctorId 等读方法

// ❌ 绝不直接注入别人的 Mapper
@Autowired private ElderlyMapper elderlyMapper;  // 禁止！
```

### 铁律3：公共设施 A 独建，B/C 只使用
> common/、pom.xml、application.yml、SecurityConfig、登录注册 都是 A 的范畴。B/C 不修改这些文件，有需求告知 A。

## 五、C 的数据库表清单（12 张）

| 表 | 用途 |
|------|------|
| warning_rule | 预警规则配置（指标类型、阈值上下限、预警级别） |
| warning_record | 预警记录（状态流转 0待处理/1处理中/2已完成/3已关闭） |
| assessment_template | 评估模板（模板名、维度数、总分） |
| assessment_dimension | 评估维度（维度名、满分、权重、评分标准JSON） |
| assessment_report | 评估报告（总分、等级、建议、状态 0草稿/1正式） |
| assessment_score | 评分明细（关联 report_id + dimension_id） |
| device | 设备台账（状态 1在线/2离线/3维修中/4已报废） |
| device_monitor_log | 设备监控日志（event_type 1上线/2离线/3数据上报/4状态变更） |
| elderly_tag | 老人标签（独居/失能/慢性病/高龄/贫困） |
| elderly_tag_mapping | 老人-标签关联 |
| followup_plan | 随访计划（状态 0待执行/1执行中/2已完成/3已逾期） |
| followup_record | 随访记录（随访方式 1电话/2上门/3门诊） |

## 六、前端代码规范

### 6.1 目录结构
```
frontend/src/
├── api/device.js       ← C的 API 封装
├── views/doctor/       ← 医生端页面
│   ├── WarningManage.vue
│   ├── FollowupManage.vue
│   └── ...
├── views/device/       ← 设备管理员页面
│   ├── DeviceManage.vue
│   └── ...
├── utils/request.js    ← axios 封装（A维护）
└── router/index.js     ← 路由（A维护）
```

### 6.2 Vue 组件模板
```vue
<template>
  <div class="page-name">
    <!-- 面包屑 -->
    <el-breadcrumb>...</el-breadcrumb>
    <!-- 标题 -->
    <h2>页面标题</h2>
    <!-- 筛选栏 -->
    <el-card><el-form :inline="true">...</el-form></el-card>
    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" stripe border v-loading="loading">
        <el-table-column ... />
      </el-table>
      <el-pagination ... />
    </el-card>
    <!-- 弹窗 -->
    <el-dialog v-model="dialog.visible">...</el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getXxxPage } from '@/api/xxx'

// 必须用 <script setup>
// API 调用必须通过 api/*.js 文件，不直接在组件里写 axios
</script>
```

### 6.3 API 封装规范
```javascript
import request from '@/utils/request.js'

export const getDevicePage = (params) => {
  return request({ url: '/api/device/page', method: 'get', params })
}
// 命名：getXxx / addXxx / updateXxx / deleteXxx
```

### 6.4 状态标签颜色
```javascript
// el-tag type:
// 预警状态: 待处理=warning, 处理中=primary, 已完成=success, 已关闭=info
// 预警级别: 轻度=info, 中度=warning, 重度=danger
// 设备状态: 在线=success, 离线=danger, 维修中=warning, 已报废=info
// 随访状态: 待执行=info, 执行中=primary, 已完成=success, 已逾期=danger
```

## 七、编码禁令

1. **禁止** 在 Controller 里写业务逻辑（判断、计算、状态机）
2. **禁止** 直接 new 假实体对象传给 Service 方法
3. **禁止** 跨域直接调用别人的 Mapper
4. **禁止** Service 方法返回 `void`（必须返回 `CommonResult` 或实体/统计值）
5. **禁止** Controller 方法里 try-catch（全局异常处理兜底）
6. **禁止** 在 Vue 组件里直接写 `axios.get/post`（必须走 `api/*.js`）
7. **禁止** 修改 `pom.xml`、`application.yml`、`common/`、`aop/` 下的文件（A 的领域）
8. **禁止** Entity 里手写 getter/setter（用 `@Data`）
9. **禁止** 用字符串拼接 SQL（用 MyBatisPlus LambdaQueryWrapper 或 XML）
10. **禁止** 提交编译不通过的代码

## 八、开发前检查

- [ ] 这个表属于我吗？（查分工表）
- [ ] 需要跨域读数据吗？→ 注入对方的 Service
- [ ] 方法对外暴露吗？→ 返回 `CommonResult`
- [ ] 写操作记录日志了吗？
- [ ] 状态机校验完整性？
- [ ] 前端 API 封装了吗？（`api/*.js`）
- [ ] `mvn compile` 通过了吗？
