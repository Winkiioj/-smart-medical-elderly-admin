package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 老人标签表
 *
 * @author C
 */
@Data
@TableName("elderly_tag")
public class ElderlyTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名（独居、失能、慢性病、高龄、贫困） */
    private String tagName;

    /** 标签颜色 */
    private String color;

    /** 标签说明 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;
}
