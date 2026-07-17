package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 健康数据记录表
 */
@Data
@TableName("health_record")
public class HealthRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long elderlyId;            // 老人ID → elderly.id

    private LocalDate measureDate;     // 测量日期

    private LocalTime measureTime;     // 测量时间

    private Integer systolicPressure;  // 收缩压(mmHg)

    private Integer diastolicPressure; // 舒张压(mmHg)

    private Integer heartRate;         // 心率(bpm)

    private BigDecimal bloodSugar;     // 血糖(mmol/L)

    private Integer bloodOxygen;       // 血氧(%)

    private BigDecimal weight;         // 体重(kg)

    private BigDecimal height;         // 身高(cm)

    private BigDecimal bmi;            // BMI（自动计算 = 体重/身高²）

    private BigDecimal temperature;    // 体温(℃)

    private String remark;             // 备注

    private String attachmentUrl;      // 体检报告附件(图片/PDF)

    private Long createBy;             // 录入人ID → sys_user.id

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
