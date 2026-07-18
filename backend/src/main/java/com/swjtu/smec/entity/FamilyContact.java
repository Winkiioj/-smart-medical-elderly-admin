package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家属联系人表
 */
@Data
@TableName("family_member")
public class FamilyContact {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long elderlyId;           // 关联老人ID → elderly.id

    private String name;              // 家属姓名

    private String relationship;      // 关系：子女/配偶/兄弟姐妹/其他

    private String phone;             // 联系电话

    private String backupPhone;       // 备用电话

    private Integer isEmergency;      // 0=否 1=是（紧急联系人）

    private String address;           // 家属住址

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
