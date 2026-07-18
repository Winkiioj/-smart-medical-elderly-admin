-- ============================================================
-- 智慧医养大数据公共服务平台 — 数据库初始化脚本
-- 数据库：MySQL 8.0 | 字符集：utf8mb4 | 引擎：InnoDB
-- 创建日期：2026-07-15
-- ============================================================

CREATE DATABASE IF NOT EXISTS `smart_elderly_care`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `smart_elderly_care`;

-- ============================================================
-- 第一部分：用户与权限域（5 张表）
-- ============================================================

-- 表 1：sys_user — 系统用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '用户ID',
    `username`        VARCHAR(50)  NOT NULL                 COMMENT '登录用户名',
    `password`        VARCHAR(255) NOT NULL                 COMMENT 'BCrypt加密密码',
    `real_name`       VARCHAR(50)  NOT NULL                 COMMENT '真实姓名',
    `phone`           VARCHAR(20)  DEFAULT NULL             COMMENT '手机号',
    `email`           VARCHAR(100) DEFAULT NULL             COMMENT '邮箱',
    `gender`          TINYINT      DEFAULT 0                COMMENT '0未知 1男 2女',
    `avatar`          VARCHAR(255) DEFAULT NULL             COMMENT '头像URL',
    `community`       VARCHAR(100) DEFAULT NULL             COMMENT '所属社区（数据权限隔离关键字段）',
    `status`          TINYINT      DEFAULT 1                COMMENT '0禁用 1启用',
    `last_login_time` DATETIME     DEFAULT NULL             COMMENT '最后登录时间',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`      TINYINT      DEFAULT 0                COMMENT '0正常 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_real_name` (`real_name`),
    KEY `idx_community` (`community`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';


-- 表 2：sys_role — 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_code`   VARCHAR(50)  NOT NULL                COMMENT 'ROLE_ORG/ROLE_COMMUNITY/ROLE_DOCTOR/ROLE_DEVICE',
    `role_name`   VARCHAR(50)  NOT NULL                COMMENT '系统管理员/社区医生/设备管理员/机构管理员',
    `description` VARCHAR(255) DEFAULT NULL            COMMENT '角色描述',
    `status`      TINYINT      DEFAULT 1               COMMENT '0禁用 1启用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';


-- 表 3：sys_menu — 菜单权限表
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id`  BIGINT       DEFAULT 0                COMMENT '父菜单ID，0=顶级',
    `menu_name`  VARCHAR(50)  NOT NULL                 COMMENT '菜单名称',
    `path`       VARCHAR(200) DEFAULT NULL             COMMENT '前端路由路径',
    `component`  VARCHAR(200) DEFAULT NULL             COMMENT '前端组件路径',
    `perms`      VARCHAR(100) DEFAULT NULL             COMMENT '权限标识符(如elderly:delete)',
    `icon`       VARCHAR(50)  DEFAULT NULL             COMMENT '图标',
    `menu_type`  TINYINT      DEFAULT NULL             COMMENT '0目录 1菜单 2按钮',
    `sort_order` INT          DEFAULT 0                COMMENT '排序',
    `status`     TINYINT      DEFAULT 1                COMMENT '0禁用 1启用',
    `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';


-- 表 4：sys_user_role — 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `role_id`     BIGINT   NOT NULL                COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';


-- 表 5：sys_role_menu — 角色菜单关联表
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id`     BIGINT   NOT NULL                COMMENT '角色ID',
    `menu_id`     BIGINT   NOT NULL                COMMENT '菜单ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';


-- ============================================================
-- 第二部分：老人档案域（2 张表）
-- ============================================================

-- 表 6：elderly — 老人基本信息表
DROP TABLE IF EXISTS `elderly`;
CREATE TABLE `elderly` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '老人ID',
    `name`              VARCHAR(50)  NOT NULL                COMMENT '姓名',
    `id_card`           VARCHAR(18)  NOT NULL                COMMENT '身份证号',
    `gender`            TINYINT      NOT NULL                COMMENT '1男 2女',
    `birth_date`        DATE         NOT NULL                COMMENT '出生日期（身份证自动提取）',
    `age`               INT          DEFAULT NULL            COMMENT '年龄（出生日期计算）',
    `phone`             VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
    `address`           VARCHAR(255) DEFAULT NULL            COMMENT '居住地址',
    `community`         VARCHAR(100) NOT NULL                COMMENT '所属社区',
    `doctor_id`         BIGINT       DEFAULT NULL            COMMENT '签约医生ID',
    `admission_date`    DATE         NOT NULL                COMMENT '入档日期',
    `height`            DECIMAL(5,1) DEFAULT NULL            COMMENT '身高(cm)',
    `emergency_contact` VARCHAR(50)  DEFAULT NULL            COMMENT '紧急联系人姓名',
    `emergency_phone`   VARCHAR(20)  DEFAULT NULL            COMMENT '紧急联系人电话',
    `medical_history`   TEXT         DEFAULT NULL            COMMENT '既往病史（逗号分隔）',
    `remark`            VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    `status`            TINYINT      DEFAULT 1               COMMENT '0离院 1在院',
    `create_by`         BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        TINYINT      DEFAULT 0               COMMENT '0正常 1已删除（软删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_id_card` (`id_card`),
    KEY `idx_name` (`name`),
    KEY `idx_community` (`community`),
    KEY `idx_doctor_id` (`doctor_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人基本信息表';


-- 表 7：family_member — 家属信息表
DROP TABLE IF EXISTS `family_member`;
CREATE TABLE `family_member` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `elderly_id`    BIGINT       NOT NULL                COMMENT '关联老人ID',
    `name`          VARCHAR(50)  NOT NULL                COMMENT '家属姓名',
    `relationship`  VARCHAR(20)  NOT NULL                COMMENT '关系：子女/配偶/兄弟姐妹/其他',
    `phone`         VARCHAR(20)  NOT NULL                COMMENT '联系电话',
    `backup_phone`  VARCHAR(20)  DEFAULT NULL            COMMENT '备用电话',
    `is_emergency`  TINYINT      DEFAULT 0               COMMENT '0否 1是（紧急联系人）',
    `address`       VARCHAR(255) DEFAULT NULL            COMMENT '家属住址',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_elderly_id` (`elderly_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='家属信息表';


-- ============================================================
-- 第三部分：健康数据域（1 张表）
-- ============================================================

-- 表 8：health_record — 健康数据记录表
DROP TABLE IF EXISTS `health_record`;
CREATE TABLE `health_record` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `elderly_id`         BIGINT       NOT NULL                COMMENT '老人ID',
    `measure_date`       DATE         NOT NULL                COMMENT '测量日期',
    `measure_time`       TIME         DEFAULT NULL            COMMENT '测量时间',
    `systolic_pressure`  INT          DEFAULT NULL            COMMENT '收缩压(mmHg)',
    `diastolic_pressure` INT          DEFAULT NULL            COMMENT '舒张压(mmHg)',
    `heart_rate`         INT          DEFAULT NULL            COMMENT '心率(bpm)',
    `blood_sugar`        DECIMAL(4,1) DEFAULT NULL            COMMENT '血糖(mmol/L)',
    `blood_oxygen`       INT          DEFAULT NULL            COMMENT '血氧(%)',
    `weight`             DECIMAL(5,1) DEFAULT NULL            COMMENT '体重(kg)',
    `height`             DECIMAL(5,1) DEFAULT NULL            COMMENT '身高(cm)',
    `bmi`                DECIMAL(4,1) DEFAULT NULL            COMMENT 'BMI（自动计算：体重/身高²）',
    `temperature`        DECIMAL(3,1) DEFAULT NULL            COMMENT '体温(℃)',
    `remark`             VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    `attachment_url`     VARCHAR(500) DEFAULT NULL            COMMENT '体检报告附件(图片/PDF)',
    `create_by`          BIGINT       DEFAULT NULL            COMMENT '录入人ID',
    `create_time`        DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_elderly_measure` (`elderly_id`, `measure_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='健康数据记录表';


-- ============================================================
-- 第四部分：健康预警域（2 张表）
-- ============================================================

-- 表 9：warning_rule — 预警规则表
DROP TABLE IF EXISTS `warning_rule`;
CREATE TABLE `warning_rule` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `rule_name`      VARCHAR(100)  NOT NULL                COMMENT '规则名称（如：收缩压重度预警）',
    `indicator_type` VARCHAR(30)   NOT NULL                COMMENT '指标类型：systolic_pressure/diastolic_pressure/heart_rate/blood_sugar/blood_oxygen/bmi/temperature',
    `indicator_name` VARCHAR(30)   NOT NULL                COMMENT '指标中文名（收缩压/血糖...）',
    `min_value`      DECIMAL(8,2)  DEFAULT NULL            COMMENT '阈值下限（低于此值触发）',
    `max_value`      DECIMAL(8,2)  DEFAULT NULL            COMMENT '阈值上限（高于此值触发）',
    `alert_level`    TINYINT       NOT NULL                COMMENT '预警级别：1轻度 2中度 3重度',
    `scope_type`     TINYINT       DEFAULT 1               COMMENT '适用范围：1全部 2指定社区 3指定标签',
    `scope_value`    VARCHAR(500)  DEFAULT NULL            COMMENT '范围值（社区名或标签ID，逗号分隔）',
    `is_enabled`     TINYINT       DEFAULT 1               COMMENT '0停用 1启用',
    `create_by`      BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `create_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警规则表';


-- 表 10：warning_record — 预警记录表
DROP TABLE IF EXISTS `warning_record`;
CREATE TABLE `warning_record` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `elderly_id`        BIGINT       NOT NULL                COMMENT '老人ID',
    `rule_id`           BIGINT       DEFAULT NULL            COMMENT '触发的规则ID',
    `health_record_id`  BIGINT       DEFAULT NULL            COMMENT '触发预警的健康数据ID',
    `alert_type`        VARCHAR(30)  NOT NULL                COMMENT '预警类型（同indicator_type）',
    `alert_level`       TINYINT      NOT NULL                COMMENT '预警级别：1轻度 2中度 3重度',
    `alert_title`       VARCHAR(200) NOT NULL                COMMENT '预警标题（如：张奶奶-收缩压重度超标）',
    `trigger_value`     VARCHAR(50)  NOT NULL                COMMENT '触发时的指标值（如：178mmHg）',
    `threshold_value`   VARCHAR(50)  DEFAULT NULL            COMMENT '阈值（如：>160mmHg）',
    `status`            TINYINT      DEFAULT 0               COMMENT '0待处理 1处理中 2已完成 3已关闭',
    `handler_id`        BIGINT       DEFAULT NULL            COMMENT '处理人ID',
    `handle_opinion`    TEXT         DEFAULT NULL            COMMENT '处理意见',
    `handle_result`     VARCHAR(500) DEFAULT NULL            COMMENT '处理结果',
    `handle_time`       DATETIME     DEFAULT NULL            COMMENT '处理时间',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '触发时间',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_elderly_rule` (`elderly_id`, `rule_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警记录表';


-- ============================================================
-- 第五部分：评估报告域（4 张表）
-- ============================================================

-- 表 11：assessment_template — 评估模板表
DROP TABLE IF EXISTS `assessment_template`;
CREATE TABLE `assessment_template` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_name`   VARCHAR(100) NOT NULL                COMMENT '模板名称（如：老年综合健康评估一级）',
    `description`     VARCHAR(500) DEFAULT NULL            COMMENT '模板说明',
    `dimension_count` INT          DEFAULT 0               COMMENT '评估维度数',
    `full_score`      INT          DEFAULT 0               COMMENT '总分（各维度满分之和）',
    `status`          TINYINT      DEFAULT 1               COMMENT '0停用 1启用',
    `create_by`       BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估模板表';


-- 表 12：assessment_dimension — 评估维度表
DROP TABLE IF EXISTS `assessment_dimension`;
CREATE TABLE `assessment_dimension` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `template_id`     BIGINT        NOT NULL                COMMENT '模板ID',
    `dimension_name`  VARCHAR(100)  NOT NULL                COMMENT '维度名（日常生活能力、认知功能...）',
    `max_score`       INT           NOT NULL                COMMENT '该维度满分',
    `weight`          DECIMAL(3,2)  DEFAULT 1.00            COMMENT '权重',
    `scoring_guide`   TEXT          DEFAULT NULL            COMMENT '评分标准说明(JSON)',
    `sort_order`      INT           DEFAULT 0               COMMENT '排序',
    `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估维度表';


-- 表 13：assessment_report — 评估报告表
DROP TABLE IF EXISTS `assessment_report`;
CREATE TABLE `assessment_report` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `elderly_id`  BIGINT        NOT NULL                COMMENT '老人ID',
    `template_id` BIGINT        NOT NULL                COMMENT '模板ID',
    `doctor_id`   BIGINT        NOT NULL                COMMENT '评估医生ID',
    `total_score` DECIMAL(5,1)  DEFAULT NULL            COMMENT '总评分',
    `full_score`  INT           DEFAULT NULL            COMMENT '满分',
    `score_level` VARCHAR(20)   DEFAULT NULL            COMMENT '等级：优秀/良好/一般/较差',
    `suggestion`  TEXT          DEFAULT NULL            COMMENT '综合建议',
    `status`      TINYINT       DEFAULT 0               COMMENT '0草稿 1正式发布',
    `pdf_url`     VARCHAR(500)  DEFAULT NULL            COMMENT 'PDF文件路径',
    `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_elderly_template` (`elderly_id`, `template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估报告表';


-- 表 14：assessment_score — 评估打分明细表
DROP TABLE IF EXISTS `assessment_score`;
CREATE TABLE `assessment_score` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `report_id`      BIGINT       NOT NULL                COMMENT '报告ID',
    `dimension_id`   BIGINT       NOT NULL                COMMENT '维度ID',
    `dimension_name` VARCHAR(100) NOT NULL                COMMENT '维度名（冗余，方便查询）',
    `score`          INT          NOT NULL                COMMENT '实际得分',
    `max_score`      INT          NOT NULL                COMMENT '该维度满分',
    `comment`        VARCHAR(500) DEFAULT NULL            COMMENT '评语',
    PRIMARY KEY (`id`),
    KEY `idx_report_id` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评估打分明细表';


-- ============================================================
-- 第六部分：设备管理域（2 张表）
-- ============================================================

-- 表 15：device — 设备台账表
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_no`         VARCHAR(50)  NOT NULL                COMMENT '设备编号（如：DEV-2026-001）',
    `device_name`       VARCHAR(100) NOT NULL                COMMENT '设备名称',
    `device_type`       VARCHAR(30)  NOT NULL                COMMENT '设备类型：血压计/血糖仪/血氧仪/智能腕表/体温计/其他',
    `brand`             VARCHAR(50)  DEFAULT NULL            COMMENT '品牌',
    `model`             VARCHAR(50)  DEFAULT NULL            COMMENT '型号',
    `community`         VARCHAR(100) DEFAULT NULL            COMMENT '所属社区',
    `elderly_id`        BIGINT       DEFAULT NULL            COMMENT '分配给哪位老人（可空）',
    `status`            TINYINT      DEFAULT 1               COMMENT '1在线 2离线 3维修中 4已报废',
    `buy_date`          DATE         DEFAULT NULL            COMMENT '购置日期',
    `warranty_end`      DATE         DEFAULT NULL            COMMENT '保修截止日期',
    `last_report_time`  DATETIME     DEFAULT NULL            COMMENT '最后上报时间（判离线用）',
    `remark`            VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    `create_by`         BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_device_no` (`device_no`),
    KEY `idx_type` (`device_type`),
    KEY `idx_status` (`status`),
    KEY `idx_community` (`community`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备台账表';


-- 表 16：device_monitor_log — 设备监控日志表
DROP TABLE IF EXISTS `device_monitor_log`;
CREATE TABLE `device_monitor_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `device_id`   BIGINT       NOT NULL                COMMENT '设备ID',
    `event_type`  TINYINT      NOT NULL                COMMENT '事件类型：1上线 2离线 3数据上报 4状态变更',
    `old_status`  TINYINT      DEFAULT NULL            COMMENT '变更前状态',
    `new_status`  TINYINT      DEFAULT NULL            COMMENT '变更后状态',
    `message`     VARCHAR(500) DEFAULT NULL            COMMENT '事件描述',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '事件时间',
    PRIMARY KEY (`id`),
    KEY `idx_device_time` (`device_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备监控日志表';


-- ============================================================
-- 第七部分：标签与随访域（4 张表）
-- ============================================================

-- 表 17：elderly_tag — 老人标签表
DROP TABLE IF EXISTS `elderly_tag`;
CREATE TABLE `elderly_tag` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tag_name`    VARCHAR(30)  NOT NULL                COMMENT '标签名（独居、失能、慢性病、高龄、贫困）',
    `color`       VARCHAR(20)  DEFAULT '#409EFF'       COMMENT '标签颜色（Hex值）',
    `description` VARCHAR(200) DEFAULT NULL            COMMENT '标签说明',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人标签表';


-- 表 18：elderly_tag_mapping — 老人标签关联表
DROP TABLE IF EXISTS `elderly_tag_mapping`;
CREATE TABLE `elderly_tag_mapping` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `elderly_id`  BIGINT   NOT NULL                COMMENT '老人ID',
    `tag_id`      BIGINT   NOT NULL                COMMENT '标签ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_elderly_tag` (`elderly_id`, `tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='老人标签关联表';


-- 表 19：followup_plan — 随访计划表
DROP TABLE IF EXISTS `followup_plan`;
CREATE TABLE `followup_plan` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `elderly_id`       BIGINT       NOT NULL                COMMENT '老人ID',
    `plan_date`        DATE         NOT NULL                COMMENT '计划随访日期',
    `followup_type`    TINYINT      NOT NULL                COMMENT '随访类型：1电话 2上门 3门诊',
    `followup_content` VARCHAR(500) DEFAULT NULL            COMMENT '随访内容/目的',
    `doctor_id`        BIGINT       DEFAULT NULL            COMMENT '执行医生ID',
    `status`           TINYINT      DEFAULT 0               COMMENT '0待执行 1执行中 2已完成 3已逾期',
    `next_plan_date`   DATE         DEFAULT NULL            COMMENT '建议下次随访日期',
    `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_elderly_plan_date` (`elderly_id`, `plan_date`),
    KEY `idx_status_plan_date` (`status`, `plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='随访计划表';


-- 表 20：followup_record — 随访记录表
DROP TABLE IF EXISTS `followup_record`;
CREATE TABLE `followup_record` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `plan_id`         BIGINT       NOT NULL                COMMENT '随访计划ID',
    `elderly_id`      BIGINT       NOT NULL                COMMENT '老人ID（冗余，方便查询）',
    `followup_date`   DATE         NOT NULL                COMMENT '实际随访日期',
    `followup_type`   TINYINT      NOT NULL                COMMENT '随访方式：1电话 2上门 3门诊',
    `elderly_status`  VARCHAR(500) DEFAULT NULL            COMMENT '老人当前状态描述',
    `intervention`    TEXT         DEFAULT NULL            COMMENT '干预措施/随访内容记录',
    `result`          VARCHAR(500) DEFAULT NULL            COMMENT '随访结果',
    `next_plan_date`  DATE         DEFAULT NULL            COMMENT '下次随访建议日期',
    `doctor_id`       BIGINT       DEFAULT NULL            COMMENT '执行医生ID',
    `attachment_url`  VARCHAR(500) DEFAULT NULL            COMMENT '附件（拍照等）',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_plan_id` (`plan_id`),
    KEY `idx_elderly_id` (`elderly_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='随访记录表';


-- ============================================================
-- 第八部分：消息通知域（1 张表）
-- ============================================================

-- 表 21：notification — 消息通知表
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`      BIGINT       NOT NULL                COMMENT '接收人ID',
    `title`        VARCHAR(200) NOT NULL                COMMENT '消息标题',
    `content`      TEXT         DEFAULT NULL            COMMENT '消息内容',
    `type`         TINYINT      NOT NULL                COMMENT '消息类型：1预警通知 2随访提醒 3系统通知 4建档通知',
    `related_id`   BIGINT       DEFAULT NULL            COMMENT '关联业务ID（如预警记录ID）',
    `related_type` VARCHAR(30)  DEFAULT NULL            COMMENT '关联业务类型（warning_record/followup_plan...）',
    `is_read`      TINYINT      DEFAULT 0               COMMENT '0未读 1已读',
    `read_time`    DATETIME     DEFAULT NULL            COMMENT '阅读时间',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';


-- ============================================================
-- 外键约束（可选，若使用物理外键则取消注释）
-- 注意事项：
--   1. 表创建顺序已考虑外键依赖，按本文顺序执行即可
--   2. 部分外键在开发阶段可暂不启用，通过应用层保证数据一致性
-- ============================================================

-- 用户-角色
-- ALTER TABLE `sys_user_role` ADD CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`id`);
-- ALTER TABLE `sys_user_role` ADD CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`);

-- 角色-菜单
-- ALTER TABLE `sys_role_menu` ADD CONSTRAINT `fk_role_menu_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`id`);
-- ALTER TABLE `sys_role_menu` ADD CONSTRAINT `fk_role_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu`(`id`);

-- 老人-家属
-- ALTER TABLE `family_member` ADD CONSTRAINT `fk_family_elderly` FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`);

-- 健康数据-老人
-- ALTER TABLE `health_record` ADD CONSTRAINT `fk_health_elderly` FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`);

-- 预警记录
-- ALTER TABLE `warning_record` ADD CONSTRAINT `fk_warning_elderly` FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`);
-- ALTER TABLE `warning_record` ADD CONSTRAINT `fk_warning_rule` FOREIGN KEY (`rule_id`) REFERENCES `warning_rule`(`id`);
-- ALTER TABLE `warning_record` ADD CONSTRAINT `fk_warning_health` FOREIGN KEY (`health_record_id`) REFERENCES `health_record`(`id`);

-- 评估
-- ALTER TABLE `assessment_dimension` ADD CONSTRAINT `fk_dimension_template` FOREIGN KEY (`template_id`) REFERENCES `assessment_template`(`id`);
-- ALTER TABLE `assessment_report` ADD CONSTRAINT `fk_report_elderly` FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`);
-- ALTER TABLE `assessment_report` ADD CONSTRAINT `fk_report_template` FOREIGN KEY (`template_id`) REFERENCES `assessment_template`(`id`);
-- ALTER TABLE `assessment_score` ADD CONSTRAINT `fk_score_report` FOREIGN KEY (`report_id`) REFERENCES `assessment_report`(`id`);
-- ALTER TABLE `assessment_score` ADD CONSTRAINT `fk_score_dimension` FOREIGN KEY (`dimension_id`) REFERENCES `assessment_dimension`(`id`);

-- 设备
-- ALTER TABLE `device_monitor_log` ADD CONSTRAINT `fk_monitor_device` FOREIGN KEY (`device_id`) REFERENCES `device`(`id`);

-- 标签
-- ALTER TABLE `elderly_tag_mapping` ADD CONSTRAINT `fk_tag_elderly` FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`);
-- ALTER TABLE `elderly_tag_mapping` ADD CONSTRAINT `fk_tag_tag` FOREIGN KEY (`tag_id`) REFERENCES `elderly_tag`(`id`);

-- 随访
-- ALTER TABLE `followup_plan` ADD CONSTRAINT `fk_plan_elderly` FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`);
-- ALTER TABLE `followup_record` ADD CONSTRAINT `fk_record_plan` FOREIGN KEY (`plan_id`) REFERENCES `followup_plan`(`id`);
-- ALTER TABLE `followup_record` ADD CONSTRAINT `fk_record_elderly` FOREIGN KEY (`elderly_id`) REFERENCES `elderly`(`id`);
