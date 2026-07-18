package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 老人基本信息表 — B 负责
 *
 * @author B
 */
@Data
@TableName("elderly")
public class Elderly {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 姓名 */
    private String name;

    /** 身份证号（UK） */
    private String idCard;

    /** 性别：1=男 2=女（身份证自动解析） */
    private Integer gender;

    /** 出生日期（身份证自动提取） */
    private LocalDate birthDate;

    /** 年龄（由birthDate计算） */
    private Integer age;

    /** 联系电话 */
    private String phone;

    /** 居住地址 */
    private String address;

    /** 所属社区 */
    private String community;

    /** 签约医生ID → sys_user.id */
    private Long doctorId;

    /** 入档日期 */
    private LocalDate admissionDate;

    /** 身高(cm) */
    private BigDecimal height;

    /** 紧急联系人姓名 */
    private String emergencyContact;

    /** 紧急联系人电话 */
    private String emergencyPhone;

    /** 既往病史（逗号分隔） */
    private String medicalHistory;

    /** 备注 */
    private String remark;

    /** 状态：0=离院 1=在院 */
    private Integer status;

    /** 创建人ID */
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
