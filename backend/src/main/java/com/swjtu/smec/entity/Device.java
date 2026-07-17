package com.swjtu.smec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备台账表 — 对应 device 表
 *
 * @author C
 */
@Data
@TableName("device")
public class Device {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 设备编号（如 DEV-20260717-001） */
    private String deviceNo;

    /** 设备名称 */
    private String deviceName;

    /** 设备类型：血压计/血糖仪/血氧仪/智能腕表/体温计/其他 */
    private String deviceType;

    /** 品牌 */
    private String brand;

    /** 型号 */
    private String model;

    /** 所属社区 */
    private String community;

    /** 分配老人ID（可空） */
    private Long elderlyId;

    /** 状态：1在线 2离线 3维修中 4已报废 */
    private Integer status;

    /** 购置日期 */
    private LocalDate buyDate;

    /** 保修截止日期 */
    private LocalDate warrantyEnd;

    /** 最后上报时间（判离线用） */
    private LocalDateTime lastReportTime;

    /** 备注 */
    private String remark;

    /** 创建人ID */
    private Long createBy;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
