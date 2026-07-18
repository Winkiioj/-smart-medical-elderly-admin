package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.entity.DeviceMonitorLog;

import java.util.List;

/**
 * 设备监控日志 Service 接口
 *
 * @author C
 */
public interface DeviceMonitorLogService extends IService<DeviceMonitorLog> {

    /**
     * 查询某设备最近 N 条日志
     */
    List<DeviceMonitorLog> getRecentLogs(Long deviceId, int limit);

    /**
     * 记录一条事件
     */
    DeviceMonitorLog logEvent(Long deviceId, Integer eventType, Integer oldStatus, Integer newStatus, String message);
}
