package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Device;

/**
 * 设备台账 Service 接口
 *
 * @author C
 */
public interface DeviceService extends IService<Device> {

    /**
     * 分页查询设备列表
     */
    CommonResult pageList(int pageNo, int pageSize,
                          String keyword, String deviceType, Integer status, String community);

    /**
     * 设备详情（含操作日志）
     */
    CommonResult getDetail(Long id);

    /**
     * 录入新设备（自动生成编号）
     */
    CommonResult add(Device device, Long operatorId);

    /**
     * 编辑设备信息（乐观锁校验 updateTime）
     */
    CommonResult updateInfo(Device device);

    /**
     * 设备状态变更
     * @param id 设备ID
     * @param newStatus 新状态(1在线/2离线/3维修中/4已报废)
     * @param operatorId 操作人ID
     */
    CommonResult updateStatus(Long id, Integer newStatus, Long operatorId);

    /**
     * 医生提交设备报修（设备改为维修中 + 记录故障信息）
     */
    CommonResult submitRepair(Long deviceId, String faultType, String faultDesc, Long reporterId, String reporterName);

    /**
     * 设备统计（复用 Dashboard 统计部分）
     */
    CommonResult getStats();

    /**
     * 设备管理员 Dashboard 聚合数据
     */
    CommonResult getDashboard(int repairPageNo, int repairPageSize);

    /**
     * 按状态统计设备数量
     */
    int countByStatus(Integer status);
    CommonResult getRepairRecords();
}
