package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社区表 — A 负责
 *
 * @author A
 */
@Data
@TableName("community")
public class Community {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 社区名称（UK） */
    private String name;

    /** 社区地址 */
    private String address;

    /** 联系人姓名 */
    private String contactName;

    /** 联系人电话 */
    private String contactPhone;

    /** 0停用 1启用 */
    private Integer status;

    /** 备注 */
    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
