package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.entity.WarningRecord;

/**
 * 预警记录 Service 接口 — C 负责
 *
 * @author C
 */
public interface WarningRecordService extends IService<WarningRecord> {

    /**
     * [供 B 跨域调用] 写入一条预警记录（健康数据导入触发）
     */
    void create(WarningRecord record);

    /**
     * [供 B 跨域调用] 24h内同一老人+同一指标是否已有预警（去重用）
     */
    boolean existsWithin24h(Long elderlyId, String alertType);

    /**
     * [供 B 跨域调用] 统计某医生名下待处理/处理中预警数
     */
    int countPendingByDoctorId(Long doctorId);

    /**
     * [供 A 跨域调用] 关闭某医生的所有待处理预警（医生禁用时）
     */
    int closePendingByDoctorId(Long doctorId);
}
