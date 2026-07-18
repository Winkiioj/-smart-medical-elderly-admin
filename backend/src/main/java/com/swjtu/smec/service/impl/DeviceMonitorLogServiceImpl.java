package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.entity.DeviceMonitorLog;
import com.swjtu.smec.mapper.DeviceMonitorLogMapper;
import com.swjtu.smec.service.DeviceMonitorLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 设备监控日志 Service 实现
 *
 * @author C
 */
@Service
@Transactional
public class DeviceMonitorLogServiceImpl
        extends ServiceImpl<DeviceMonitorLogMapper, DeviceMonitorLog>
        implements DeviceMonitorLogService {

    @Override
    public List<DeviceMonitorLog> getRecentLogs(Long deviceId, int limit) {
        return this.baseMapper.selectByDeviceId(deviceId, limit);
    }

    @Override
    public DeviceMonitorLog logEvent(Long deviceId, Integer eventType, Integer oldStatus, Integer newStatus, String message) {
        DeviceMonitorLog log = new DeviceMonitorLog();
        log.setDeviceId(deviceId);
        log.setEventType(eventType);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setMessage(message);
        log.setCreateTime(java.time.LocalDateTime.now());
        this.baseMapper.insert(log);
        return log;
    }
}
