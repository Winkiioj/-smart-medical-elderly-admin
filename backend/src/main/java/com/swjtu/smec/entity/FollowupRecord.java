package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("followup_record")
public class FollowupRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long planId;
    private Long elderlyId;
    private LocalDate followupDate;
    private Integer followupType;
    private String elderlyStatus;
    private String intervention;
    private String result;
    private LocalDate nextPlanDate;
    private Long doctorId;
    private String attachmentUrl;
    private LocalDateTime createTime;
}
