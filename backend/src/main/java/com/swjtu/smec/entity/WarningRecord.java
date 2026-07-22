package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("warning_record")
public class WarningRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderlyId;
    private Long ruleId;
    private Long healthRecordId;
    private String alertType;
    private Integer alertLevel;
    private String alertTitle;
    private String triggerValue;
    private String thresholdValue;
    private Integer status;
    private Long handlerId;
    private String handleOpinion;
    private String handleResult;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String elderlyName;

    @TableField(exist = false)
    private String handlerName;
}
