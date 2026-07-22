package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Elderly;
import com.swjtu.smec.entity.FollowupPlan;
import com.swjtu.smec.entity.FollowupRecord;
import com.swjtu.smec.mapper.FollowupPlanMapper;
import com.swjtu.smec.service.ElderlyService;
import com.swjtu.smec.service.FollowupPlanService;
import com.swjtu.smec.service.FollowupRecordService;
import com.swjtu.smec.service.HealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class FollowupPlanServiceImpl
        extends ServiceImpl<FollowupPlanMapper, FollowupPlan>
        implements FollowupPlanService {

    @Autowired
    private FollowupRecordService recordService;

    @Autowired
    private HealthRecordService healthRecordService;

    @Autowired
    private ElderlyService elderlyService;

    @Override
    public CommonResult pageByDoctor(int pageNo, int pageSize, Long doctorId,
                                      Integer followupType, Integer status,
                                      String startDate, String endDate) {
        Page<FollowupPlan> page = new Page<>(pageNo, pageSize);
        IPage<FollowupPlan> result = this.baseMapper.selectPageByDoctor(
                page, doctorId, followupType, status, startDate, endDate);
        // 填充老人姓名
        for (FollowupPlan plan : result.getRecords()) {
            Elderly e = elderlyService.getById(plan.getElderlyId());
            if (e != null) plan.setElderlyName(e.getName());
        }
        return CommonResult.success(result.getRecords(), result.getTotal());
    }

    @Override
    public CommonResult create(FollowupPlan plan) {
        // 校验同老人+同类型无未完成计划
        LambdaQueryWrapper<FollowupPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowupPlan::getElderlyId, plan.getElderlyId())
               .eq(FollowupPlan::getFollowupType, plan.getFollowupType())
               .in(FollowupPlan::getStatus, 0, 1);
        if (this.baseMapper.selectCount(wrapper) > 0) {
            return CommonResult.error(400, "该老人已有未完成的同类型随访计划");
        }
        plan.setStatus(0);
        plan.setCreateTime(LocalDateTime.now());
        this.baseMapper.insert(plan);
        return CommonResult.success(plan);
    }

    @Override
    public CommonResult start(Long planId, Long doctorId) {
        FollowupPlan plan = this.baseMapper.selectById(planId);
        if (plan == null) return CommonResult.error(404, "计划不存在");
        if (plan.getStatus() != 0) return CommonResult.error(400, "当前状态不可开始");
        plan.setStatus(1);
        plan.setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(plan);
        return CommonResult.success(null);
    }

    @Override
    public CommonResult complete(Long planId, FollowupRecord record) {
        FollowupPlan plan = this.baseMapper.selectById(planId);
        if (plan == null) return CommonResult.error(404, "计划不存在");
        if (plan.getStatus() != 1) return CommonResult.error(400, "当前状态不可完成");

        // 保存随访记录
        record.setPlanId(planId);
        record.setElderlyId(plan.getElderlyId());
        record.setDoctorId(plan.getDoctorId());
        record.setFollowupType(plan.getFollowupType());
        record.setFollowupDate(LocalDate.now());
        record.setCreateTime(LocalDateTime.now());
        recordService.save(record);

        // 更新计划状态
        plan.setStatus(2);
        plan.setNextPlanDate(record.getNextPlanDate());
        plan.setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(plan);

        // 自动生成下次随访计划
        if (record.getNextPlanDate() != null) {
            FollowupPlan next = new FollowupPlan();
            next.setElderlyId(plan.getElderlyId());
            next.setDoctorId(plan.getDoctorId());
            next.setFollowupType(plan.getFollowupType());
            next.setPlanDate(record.getNextPlanDate());
            String conclusion = record.getResult() != null ? record.getResult() : "未知";
            next.setFollowupContent("系统自动生成 — 上次随访结论：" + conclusion + "，于" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            next.setStatus(0);
            next.setCreateTime(LocalDateTime.now());
            this.baseMapper.insert(next);
        }

        return CommonResult.success(null);
    }

    @Override
    public CommonResult updatePlanDate(Long planId, LocalDate newDate) {
        FollowupPlan plan = this.baseMapper.selectById(planId);
        if (plan == null) return CommonResult.error(404, "计划不存在");
        if (plan.getStatus() != 0) return CommonResult.error(400, "仅待执行的计划可修改日期");
        plan.setPlanDate(newDate);
        plan.setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(plan);
        return CommonResult.success(null);
    }

    @Override
    public FollowupPlan insertPlan(FollowupPlan plan) {
        this.baseMapper.insert(plan);
        return plan;
    }

    @Override
    public CommonResult getPlanById(Long planId) {
        FollowupPlan plan = this.baseMapper.selectById(planId);
        if (plan == null) return CommonResult.error(404, "计划不存在");
        return CommonResult.success(plan);
    }

    @Override
    public CommonResult countCompletedByDoctorId(Long doctorId) {
        LambdaQueryWrapper<FollowupPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FollowupPlan::getDoctorId, doctorId)
               .eq(FollowupPlan::getStatus, 2);
        int count = this.baseMapper.selectCount(wrapper).intValue();
        return CommonResult.success(count);
    }
}
