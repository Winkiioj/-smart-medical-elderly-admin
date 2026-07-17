# 智慧医养管理系统 (SMECMS)

> Smart Medical-Elderly Care Management System  
> 智慧医养大数据公共服务平台 — 云端后台管理系统（医疗板块）  
> 项目编号：HD2026SMECMS01

---

## 技术栈

| 层 | 技术 | 版本 |
|------|------|------|
| 前端 | Vue 3 + Vite + Element Plus + ECharts | Vue 3.5 / Vite 8.x |
| 后端 | Spring Boot + MyBatis Plus | Spring Boot 3.5.13 / MP 3.5.12 |
| 认证 | Spring Security + JWT + Redis | — |
| 数据库 | MySQL + Redis | MySQL 8.0 / Redis 7.x |
| 文档 | SpringDoc (Swagger 3) | 2.8.4 |

## 项目结构

```
smart-medical-elderly-admin/
├── backend/                    # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/swjtu/smec/
│       ├── Application.java      # 启动类
│       ├── entity/               # 实体类
│       ├── mapper/               # Mapper 接口
│       ├── service/              # Service 接口
│       │   └── impl/             # Service 实现
│       ├── controller/           # Controller
│       ├── common/               # 公共模块
│       │   ├── config/           # 配置（CORS、MyBatisPlus）
│       │   ├── result/           # 统一返回结果
│       │   ├── exception/        # 全局异常处理
│       │   ├── enums/            # 错误码枚举
│       │   └── annotation/       # 自定义注解
│       └── aop/                  # AOP 切面（Token校验）
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/                  # API 请求封装
│   │   ├── utils/                # 工具（axios封装、localStorage）
│   │   ├── router/               # 路由配置
│   │   ├── views/                # 页面组件
│   │   ├── components/           # 公共组件
│   │   └── styles/               # 样式
│   └── vite.config.js
├── docs/                       # 项目文档
└── README.md
```

## 快速启动

### 1. 数据库

```sql
CREATE DATABASE smec_db DEFAULT CHARACTER SET utf8mb4;
```

### 2. 后端

```bash
cd backend
mvn spring-boot:run
# 访问 http://localhost:8080/swagger-ui.html
```

### 3. 前端

```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

## 团队分工

| 成员 | 负责域 | 表 | 用例 |
|:--:|------|------|:--:|
| **A** | 认证授权 + 机构/社区管理 | sys_user/sys_role/sys_menu/notification/community (7表) | UC-ORG-01~02, UC-COM-01~03 (5用例) |
| **B** | 老人与健康数据 | elderly/family_contact/health_record (5表) | UC-DOC-01~04 (4用例) |
| **C** | 医疗服务 + 设备管理 | warning_*/followup_*/assessment_*/device* (9表) | UC-DOC-05~08, UC-DEV-01~03 (7用例) |

## 开发规范

- 分支命名：`feature/<模块名>`（如 `feature/elderly-crud`）
- 提交信息：`<type>: <描述>`（如 `feat: 老人档案增删改查`）
- 合并方式：PR → review → merge（A 负责 review）
- 跨域协作：注入对方 Service 读取，不直接调 Mapper

---

**西南交通大学 计算机与人工智能学院 软件工程专业 华迪实训**
