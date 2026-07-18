package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("assessment_template")
public class AssessmentTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateName;
    private String description;
    private Integer dimensionCount;
    private Integer fullScore;
    private Integer status;
    private Long createBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
