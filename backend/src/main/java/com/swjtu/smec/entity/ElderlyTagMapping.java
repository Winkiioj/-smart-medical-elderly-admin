package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 老人标签关联表
 *
 * @author C
 */
@Data
@TableName("elderly_tag_mapping")
public class ElderlyTagMapping {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 老人ID */
    private Long elderlyId;

    /** 标签ID */
    private Long tagId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
