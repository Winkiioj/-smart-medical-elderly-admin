package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设备监控日志表 — 对应 device_monitor_log 表
 *
 * @author C
 */
@Data
@TableName("device_monitor_log")
public class DeviceMonitorLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备ID */
    private Long deviceId;

    /** 事件类型：1上线 2离线 3数据上报 4状态变更 */
    private Integer eventType;

    /** 变更前状态 */
    private Integer oldStatus;

    /** 变更后状态 */
    private Integer newStatus;

    /** 事件描述 */
    private String message;

    /** 事件时间 */
    private LocalDateTime createTime;
}
