package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("followup_plan")
public class FollowupPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long elderlyId;
    private LocalDate planDate;
    private Integer followupType;
    private String followupContent;
    private Long doctorId;
    private Integer status;
    private LocalDate nextPlanDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
