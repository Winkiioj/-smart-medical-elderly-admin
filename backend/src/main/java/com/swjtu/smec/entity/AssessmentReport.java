package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("assessment_report")
public class AssessmentReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderlyId;
    private Long templateId;
    private Long doctorId;
    private BigDecimal totalScore;
    private Integer fullScore;
    private String scoreLevel;
    private String suggestion;
    private Integer status;
    private String pdfUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
