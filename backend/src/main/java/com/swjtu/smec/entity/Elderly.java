package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 老人档案 — A 临时使用（完整 Entity 由 B 补充）
 */
@Data
@TableName("elderly")
public class Elderly {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String idCard;

    private Integer gender;         // 1=男 2=女

    private LocalDate birthDate;

    private Integer age;

    private String phone;

    private String address;

    private String community;

    private Long doctorId;          // 签约医生ID

    private LocalDate admissionDate;

    private String emergencyContact;

    private String emergencyPhone;

    private String medicalHistory;

    private String remark;

    private Integer status;         // 1=正常

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
