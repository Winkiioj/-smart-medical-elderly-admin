package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.FollowupPlan;
import com.swjtu.smec.entity.FollowupRecord;

import java.time.LocalDate;

public interface FollowupPlanService extends IService<FollowupPlan> {
    CommonResult pageByDoctor(int pageNo, int pageSize, Long doctorId,
                              Integer followupType, Integer status,
                              String startDate, String endDate);
    CommonResult create(FollowupPlan plan);
    CommonResult start(Long planId, Long doctorId);
    CommonResult complete(Long planId, FollowupRecord record);
    CommonResult updatePlanDate(Long planId, LocalDate newDate);
    CommonResult countCompletedByDoctorId(Long doctorId);
}
