package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("assessment_score")
public class AssessmentScore {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reportId;
    private Long dimensionId;
    private String dimensionName;
    private Integer score;
    private Integer maxScore;
    private String comment;
}
