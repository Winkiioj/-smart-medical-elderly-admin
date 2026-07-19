package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.FollowupRecord;
import com.swjtu.smec.mapper.FollowupRecordMapper;
import com.swjtu.smec.service.FollowupRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FollowupRecordServiceImpl
        extends ServiceImpl<FollowupRecordMapper, FollowupRecord>
        implements FollowupRecordService {

    @Override
    public CommonResult getRecentByElderly(Long elderlyId, int limit) {
        List<FollowupRecord> list = this.baseMapper.selectRecentByElderly(elderlyId, limit);
        return CommonResult.success(list);
    }

    @Override
    public CommonResult getByPlanId(Long planId) {
        LambdaQueryWrapper<FollowupRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowupRecord::getPlanId, planId)
               .orderByDesc(FollowupRecord::getCreateTime);
        FollowupRecord record = this.baseMapper.selectOne(wrapper);
        if (record == null) return CommonResult.success(null);
        return CommonResult.success(record);
    }
}
