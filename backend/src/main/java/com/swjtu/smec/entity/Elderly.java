package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 老人基本信息表
 */
@Data
@TableName("elderly")
public class Elderly {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;              // 姓名

    private String idCard;            // 身份证号（UK）

    private Integer gender;           // 1=男 2=女（身份证自动解析）

    private LocalDate birthDate;      // 出生日期（身份证自动提取）

    private Integer age;              // 年龄（由birthDate计算）

    private String phone;             // 联系电话

    private String address;           // 居住地址

    private String community;         // 所属社区

    private Long doctorId;            // 签约医生ID → sys_user.id

    private LocalDate admissionDate;  // 入档日期

    private BigDecimal height;        // 身高(cm)

    private String emergencyContact;  // 紧急联系人姓名

    private String emergencyPhone;    // 紧急联系人电话

    private String medicalHistory;    // 既往病史（逗号分隔）

    private String remark;            // 备注

    private Integer status;           // 0=离院 1=在院

    private Long createBy;            // 创建人ID
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
    private Integer isDeleted;        // 0=正常 1=已删除（软删除）
}
