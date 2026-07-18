package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 健康数据导入日志表
 */
@Data
@TableName("import_log")
public class ImportLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String fileName;           // 原始文件名

    private Long doctorId;             // 导入医生ID → sys_user.id

    private Integer totalCount;        // 文件中的总记录数

    private Integer successCount;      // 成功导入条数

    private Integer failCount;         // 失败条数

    private String failDetail;         // 失败详情 JSON：[{index,elderlyId,reason},...]

    private Integer warningCount;      // 触发预警条数

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 导入时间
}
