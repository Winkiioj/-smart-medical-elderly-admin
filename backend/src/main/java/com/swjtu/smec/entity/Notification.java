package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知表 — A 负责
 *
 * @author A
 */
@Data
@TableName("notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收人ID → sys_user.id */
    private Long userId;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 类型：1预警通知 2随访提醒 3系统通知 4建档通知 */
    private Integer type;

    /** 关联业务ID（如预警记录ID） */
    private Long relatedId;

    /** 关联业务类型 */
    private String relatedType;

    /** 0未读 1已读 */
    private Integer isRead;

    /** 阅读时间 */
    private LocalDateTime readTime;

    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;
}
