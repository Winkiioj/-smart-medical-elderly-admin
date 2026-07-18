package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单权限表
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long parentId;       // 父菜单ID，0=顶级菜单

    private String menuName;     // 菜单名称

    private String path;         // 路由路径

    private String component;    // 前端组件路径

    private String perms;        // 权限标识

    private String icon;         // 图标

    private Integer menuType;    // 0=目录 1=菜单 2=按钮

    private Integer sortOrder;   // 排序

    private Integer status;      // 0=禁用 1=启用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
