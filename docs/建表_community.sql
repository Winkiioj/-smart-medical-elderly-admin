-- 在 Navicat 里选 smart_elderly_care 库，执行下面这段

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

-- 初始化：把 elderly 表里出现过的社区名反向填进去
INSERT INTO community (name, status)
SELECT DISTINCT community, 1 FROM elderly
WHERE community IS NOT NULL AND community != ''
  AND community NOT IN (SELECT name FROM community);
