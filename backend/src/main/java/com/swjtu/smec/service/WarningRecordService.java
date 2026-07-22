package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.WarningRecord;

/**
 * 预警记录 Service 接口
 *
 * @author C
 */
public interface WarningRecordService extends IService<WarningRecord> {

    // ===== 供 B/A 跨域调用 =====
    WarningRecord create(WarningRecord record);
    boolean existsWithin24h(Long elderlyId, String alertType);
    int countPendingByDoctorId(Long doctorId);
    int closePendingByDoctorId(Long doctorId);

    // ===== UC-DOC-05 预警处理 =====
    CommonResult pageByDoctor(int pageNo, int pageSize, Long doctorId, String community,
                              Integer alertLevel, String alertType, Integer status,
                              String startTime, String endTime);
    CommonResult getDetailWithElderly(Long id);
    CommonResult getDetail(Long id);
    CommonResult accept(Long id, Long handlerId);
    CommonResult complete(Long id, String opinion, String result);
    CommonResult close(Long id, String reason);
}
