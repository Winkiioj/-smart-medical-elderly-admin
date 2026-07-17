package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色表
 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleCode;     // 角色编码：ORG_ADMIN / COM_ADMIN / DOCTOR / DEVICE_ADMIN

    private String roleName;     // 角色名称：机构管理员 / 社区管理员 / 社区医生 / 设备管理员

    private String description;

    private Integer status;      // 0=禁用 1=启用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
