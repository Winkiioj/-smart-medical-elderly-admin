package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Device;
import com.swjtu.smec.entity.DeviceMonitorLog;
import com.swjtu.smec.mapper.DeviceMapper;
import com.swjtu.smec.mapper.DeviceMonitorLogMapper;
import com.swjtu.smec.service.DeviceMonitorLogService;
import com.swjtu.smec.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 设备台账 Service 实现
 *
 * @author C
 */
@Service
@Transactional
public class DeviceServiceImpl
        extends ServiceImpl<DeviceMapper, Device>
        implements DeviceService {

    @Autowired
    private DeviceMonitorLogService deviceMonitorLogService;

    @Autowired
    private DeviceMonitorLogMapper deviceMonitorLogMapper;

    @Override
    public CommonResult pageList(int pageNo, int pageSize,
                                  String keyword, String deviceType, Integer status, String community) {
        Page<Device> page = new Page<>(pageNo, pageSize);
        IPage<Device> result = this.baseMapper.selectPage(page, keyword, deviceType, status, community);
        return CommonResult.success(result.getRecords(), result.getTotal());
    }

    @Override
    public CommonResult getDetail(Long id) {
        Device device = this.baseMapper.selectById(id);
        if (device == null) {
            return CommonResult.error(404, "设备不存在");
        }
        // 附带最近操作日志
        return CommonResult.success(device);
    }

    @Override
    public CommonResult add(Device device, Long operatorId) {
        // 1. 校验唯一标识码
        String deviceNo = device.getDeviceNo();
        if (deviceNo != null && !deviceNo.isEmpty()) {
            Device exist = this.baseMapper.selectByDeviceNo(deviceNo);
            if (exist != null) {
                return CommonResult.error(400, "设备编号已存在：" + deviceNo);
            }
        }

        // 2. 自动生成设备编号（如未提供）
        if (deviceNo == null || deviceNo.isEmpty()) {
            device.setDeviceNo(generateDeviceNo(device.getDeviceType()));
        }

        // 3. 设置初始状态为离线
        device.setStatus(2);
        device.setCreateBy(operatorId);
        device.setCreateTime(LocalDateTime.now());

        this.baseMapper.insert(device);

        // 4. 记录操作日志
        deviceMonitorLogService.logEvent(device.getId(), 4,
                null, device.getStatus(), "设备录入");

        return CommonResult.success(device);
    }

    @Override
    public CommonResult updateInfo(Device device) {
        Device db = this.baseMapper.selectById(device.getId());
        if (db == null) {
            return CommonResult.error(404, "设备不存在");
        }

        // 编号不允许修改，某些字段保持原值
        device.setDeviceNo(null);
        device.setCreateBy(null);
        device.setCreateTime(null);
        device.setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(device);

        // 记录日志
        deviceMonitorLogService.logEvent(device.getId(), 4,
                db.getStatus(), db.getStatus(), "设备信息编辑");

        return CommonResult.success(null);
    }

    @Override
    public CommonResult updateStatus(Long id, Integer newStatus, Long operatorId) {
        Device db = this.baseMapper.selectById(id);
        if (db == null) {
            return CommonResult.error(404, "设备不存在");
        }
        // 状态值校验
        if (newStatus < 1 || newStatus > 4) {
            return CommonResult.error(400, "无效的状态值：" + newStatus);
        }
        // 报废是终态，不可再变更
        if (db.getStatus() == 4) {
            return CommonResult.error(400, "已报废设备不可变更状态");
        }

        Integer fromStatus = db.getStatus();
        db.setStatus(newStatus);
        db.setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(db);

        // 记录操作日志
        deviceMonitorLogService.logEvent(id, 4, fromStatus, newStatus,
                "状态变更 [" + operatorId + "]: " + statusText(fromStatus) + " → " + statusText(newStatus));

        return CommonResult.success(null);
    }

    @Override
    public CommonResult getStats() {
        int online = this.baseMapper.countByStatus(1);
        int offline = this.baseMapper.countByStatus(2);
        int repairing = this.baseMapper.countByStatus(3);
        int scrapped = this.baseMapper.countByStatus(4);
        return CommonResult.success(java.util.Map.of(
                "total", online + offline + repairing + scrapped,
                "online", online,
                "offline", offline,
                "repairing", repairing,
                "scrapped", scrapped
        ));
    }

    @Override
    public CommonResult getDashboard() {
        int online = this.baseMapper.countByStatus(1);
        int offline = this.baseMapper.countByStatus(2);
        int repairing = this.baseMapper.countByStatus(3);
        int scrapped = this.baseMapper.countByStatus(4);

        // 查询维修中的设备 + 最近日志
        LambdaQueryWrapper<Device> repairWrapper = new LambdaQueryWrapper<>();
        repairWrapper.eq(Device::getStatus, 3).orderByDesc(Device::getUpdateTime);
        Page<Device> repairPage = new Page<>(1, 5);
        java.util.List<Device> repairingDevices = this.baseMapper.selectPage(repairPage, repairWrapper).getRecords();

        // 查询质保即将到期设备（30天内）
        LambdaQueryWrapper<Device> warrantyWrapper = new LambdaQueryWrapper<>();
        warrantyWrapper.le(Device::getWarrantyEnd, LocalDate.now().plusDays(30))
                       .ge(Device::getWarrantyEnd, LocalDate.now())
                       .ne(Device::getStatus, 4)  // 排除已报废
                       .orderByAsc(Device::getWarrantyEnd);
        Page<Device> warrantyPage = new Page<>(1, 5);
        java.util.List<Device> expiringDevices = this.baseMapper.selectPage(warrantyPage, warrantyWrapper).getRecords();

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("stats", java.util.Map.of(
                "total", online + offline + repairing + scrapped,
                "online", online,
                "offline", offline,
                "repairing", repairing,
                "scrapped", scrapped
        ));
        result.put("repairingDevices", repairingDevices);
        result.put("expiringDevices", expiringDevices);

        return CommonResult.success(result);
    }

    @Override
    public CommonResult submitRepair(Long deviceId, String faultType, String faultDesc, Long reporterId, String reporterName) {
        Device db = this.baseMapper.selectById(deviceId);
        if (db == null) {
            return CommonResult.error(404, "设备不存在");
        }
        if (db.getStatus() == 3) {
            return CommonResult.error(400, "该设备已在维修中");
        }
        if (db.getStatus() == 4) {
            return CommonResult.error(400, "已报废设备不可报修");
        }
        Integer fromStatus = db.getStatus();
        db.setStatus(3); // 维修中
        db.setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(db);

        String msg = "医生报修[" + reporterName + "]: " + faultType + " — " + faultDesc;
        deviceMonitorLogService.logEvent(deviceId, 4, fromStatus, 3, msg);
        return CommonResult.success(null);
    }

    @Override
    public CommonResult getRepairRecords() {
        LambdaQueryWrapper<DeviceMonitorLog> w = new LambdaQueryWrapper<>();
        w.like(DeviceMonitorLog::getMessage, "报修")
         .orderByDesc(DeviceMonitorLog::getCreateTime);
        Page<DeviceMonitorLog> p = new Page<>(1, 20);
        java.util.List<DeviceMonitorLog> logs = deviceMonitorLogMapper.selectPage(p, w).getRecords();

        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (DeviceMonitorLog log : logs) {
            Device device = this.baseMapper.selectById(log.getDeviceId());
            if (device == null) continue;

            String msg = log.getMessage() != null ? log.getMessage() : "";
            String reporter = "";
            String faultType = "";
            String faultDesc = "";
            int endName = msg.indexOf("]: ");
            if (endName > 0) {
                int startName = msg.indexOf("[") + 1;
                reporter = msg.substring(startName, endName);
                String rest = msg.substring(endName + 3);
                int dash = rest.indexOf(" — ");
                if (dash > 0) {
                    faultType = rest.substring(0, dash);
                    faultDesc = rest.substring(dash + 3);
                } else {
                    faultDesc = rest;
                }
            } else {
                faultDesc = msg;
            }

            boolean exists = result.stream().anyMatch(m -> m.get("deviceId").equals(device.getId()));
            if (exists) continue;

            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("deviceId", device.getId());
            item.put("deviceNo", device.getDeviceNo());
            item.put("deviceName", device.getDeviceName());
            item.put("status", device.getStatus());
            item.put("faultType", faultType.isEmpty() ? "其他" : faultType);
            item.put("faultDesc", faultDesc.isEmpty() ? msg : faultDesc);
            item.put("reporterName", reporter.isEmpty() ? "未知" : reporter);
            item.put("repairTime", log.getCreateTime() != null ? log.getCreateTime().toString() : "");
            result.add(item);
        }
        return CommonResult.success(result);
    }

    @Override
    public int countByStatus(Integer status) {
        return this.baseMapper.countByStatus(status);
    }

    private String statusText(Integer s) {
        switch (s == null ? 0 : s) {
            case 1: return "在线";
            case 2: return "离线";
            case 3: return "维修中";
            case 4: return "已报废";
            default: return "未知";
        }
    }

    // ===== 私有方法 =====

    /**
     * 生成设备编号：DEV-{yyyyMMdd}-{序号3位}
     */
    private String generateDeviceNo(String deviceType) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        // 查询当天已录入设备数，生成序号
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(Device::getCreateTime, LocalDate.now().atStartOfDay());
        int count = this.baseMapper.selectCount(wrapper).intValue();
        String seq = String.format("%03d", count + 1);
        return "DEV-" + dateStr + "-" + seq;
    }
}
