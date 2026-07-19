package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.FollowupRecord;

public interface FollowupRecordService extends IService<FollowupRecord> {
    CommonResult getRecentByElderly(Long elderlyId, int limit);
    CommonResult getByPlanId(Long planId);
}
