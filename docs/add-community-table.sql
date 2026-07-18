-- ============================================================
-- 社区表建表语句（独立执行）
-- 执行方式：用 Navicat/DBeaver 连接 192.168.52.142:3306
--         选 smart_elderly_care 库，执行此文件
-- 或在终端执行：mysql -h 192.168.52.142 -u root -p123456 smart_elderly_care < add-community-table.sql
-- ============================================================

DROP TABLE IF EXISTS `community`;
CREATE TABLE `community` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`          VARCHAR(100) NOT NULL                COMMENT '社区名称',
    `address`       VARCHAR(255) DEFAULT NULL            COMMENT '社区地址',
    `contact_name`  VARCHAR(50)  DEFAULT NULL            COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20)  DEFAULT NULL            COMMENT '联系人电话',
    `status`        TINYINT      DEFAULT 1               COMMENT '0停用 1启用',
    `remark`        VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社区表';

-- 初始化已有社区（从 elderly/device 表中出现的社区名反向填充）
INSERT INTO community (name, address, status) VALUES
('花园社区', '成都市郫都区红光街道', 1)
ON DUPLICATE KEY UPDATE status = 1;

-- 如果其他社区名已在 elderly 中出现，自动补入
INSERT INTO community (name, status)
SELECT DISTINCT community, 1 FROM elderly
WHERE community IS NOT NULL AND community != ''
  AND community NOT IN (SELECT name FROM community)
ON DUPLICATE KEY UPDATE status = 1;
