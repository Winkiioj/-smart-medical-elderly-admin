package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("assessment_dimension")
public class AssessmentDimension {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long templateId;
    private String dimensionName;
    private Integer maxScore;
    private BigDecimal weight;
    private String scoringGuide;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
