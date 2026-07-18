package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("warning_rule")
public class WarningRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleName;
    private String indicatorType;
    private String indicatorName;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private Integer alertLevel;
    private Integer scopeType;
    private String scopeValue;
    private Integer isEnabled;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
