package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Elderly;
import com.swjtu.smec.entity.FollowupPlan;
import com.swjtu.smec.entity.WarningRecord;
import com.swjtu.smec.mapper.WarningRecordMapper;
import com.swjtu.smec.service.ElderlyService;
import com.swjtu.smec.service.FollowupPlanService;
import com.swjtu.smec.service.WarningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预警记录 Service 实现
 *
 * @author C
 */
@Service
@Transactional
public class WarningRecordServiceImpl
        extends ServiceImpl<WarningRecordMapper, WarningRecord>
        implements WarningRecordService {

    @Autowired
    private ElderlyService elderlyService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private FollowupPlanService followupPlanService;

    // ===== 供 B/A 跨域调用 =====

    @Override
    public WarningRecord create(WarningRecord record) {
        record.setCreateTime(LocalDateTime.now());
        this.baseMapper.insert(record);
        return record;
    }

    @Override
    public boolean existsWithin24h(Long elderlyId, String alertType) {
        LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRecord::getElderlyId, elderlyId)
               .eq(WarningRecord::getAlertType, alertType)
               .ge(WarningRecord::getCreateTime, LocalDateTime.now().minusHours(24));
        return this.baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int countPendingByDoctorId(Long doctorId) {
        List<Long> elderlyIds = getElderlyIdsByDoctor(doctorId);
        if (elderlyIds.isEmpty()) return 0;
        LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WarningRecord::getElderlyId, elderlyIds)
               .in(WarningRecord::getStatus, 0, 1);
        return this.baseMapper.selectCount(wrapper).intValue();
    }

    @Override
    public int closePendingByDoctorId(Long doctorId) {
        List<Long> elderlyIds = getElderlyIdsByDoctor(doctorId);
        LambdaUpdateWrapper<WarningRecord> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(WarningRecord::getStatus, 3)
               .eq(WarningRecord::getStatus, 0)
               .and(w -> w.eq(WarningRecord::getHandlerId, doctorId)
                         .or()
                         .in(WarningRecord::getElderlyId, elderlyIds));
        return this.baseMapper.update(null, wrapper);
    }

    // ===== UC-DOC-05 预警处理 =====

    @Override
    public CommonResult pageByDoctor(int pageNo, int pageSize, Long doctorId,
                                      Integer alertLevel, String alertType, Integer status,
                                      String startTime, String endTime) {
        List<Long> elderlyIds = getElderlyIdsByDoctor(doctorId);
        Page<WarningRecord> page = new Page<>(pageNo, pageSize);
        IPage<WarningRecord> result = this.baseMapper.selectPageByDoctor(
                page, elderlyIds, alertLevel, alertType, status, startTime, endTime);
        return CommonResult.success(result.getRecords(), result.getTotal());
    }

    @Override
    public CommonResult getDetail(Long id) {
        WarningRecord record = this.baseMapper.selectById(id);
        if (record == null) {
            return CommonResult.error(404, "预警记录不存在");
        }
        return CommonResult.success(record);
    }

    @Override
    public CommonResult getDetailWithElderly(Long id) {
        WarningRecord record = this.baseMapper.selectById(id);
        if (record == null) {
            return CommonResult.error(404, "预警记录不存在");
        }
        Elderly elderly = elderlyService.getById(record.getElderlyId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("warning", record);
        data.put("elderly", elderly);
        return CommonResult.success(data);
    }

    @Override
    public CommonResult accept(Long id, Long handlerId) {
        WarningRecord record = this.baseMapper.selectById(id);
        if (record == null) return CommonResult.error(404, "预警记录不存在");
        if (record.getStatus() != 0) return CommonResult.error(400, "当前状态不可接单");
        record.setStatus(1); // 处理中
        record.setHandlerId(handlerId);
        record.setHandleTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(record);

        // 自动生成关联随访计划（【预警生成】标记）
        String planMsg = "";
        try {
            FollowupPlan plan = new FollowupPlan();
            plan.setElderlyId(record.getElderlyId());
            plan.setDoctorId(handlerId);
            plan.setFollowupType(1);
            plan.setPlanDate(LocalDate.now());
            plan.setFollowupContent("【预警生成】" + record.getAlertTitle());
            CommonResult planResult = followupPlanService.create(plan);
            if (planResult.getCode() == 200) {
                planMsg = "，随访计划已自动生成。请前往随访管理执行";
            } else {
                planMsg = "。注意：" + planResult.getMsg();
            }
        } catch (Exception e) {
            planMsg = "。注意：随访计划自动生成异常，请手动创建";
        }

        return CommonResult.success("接单成功" + planMsg);
    }

    @Override
    public CommonResult complete(Long id, String opinion, String result) {
        WarningRecord record = this.baseMapper.selectById(id);
        if (record == null) return CommonResult.error(404, "预警记录不存在");
        if (record.getStatus() != 1) return CommonResult.error(400, "当前状态不可完成");
        record.setStatus(2); // 已完成
        record.setHandleOpinion(opinion);
        record.setHandleResult(result);
        record.setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(record);
        return CommonResult.success(null);
    }

    @Override
    public CommonResult close(Long id, String reason) {
        WarningRecord record = this.baseMapper.selectById(id);
        if (record == null) return CommonResult.error(404, "预警记录不存在");
        if (record.getStatus() == 2 || record.getStatus() == 3) {
            return CommonResult.error(400, "已完成/已关闭的预警不可再关闭");
        }
        record.setStatus(3); // 已关闭
        record.setHandleOpinion("关闭原因: " + reason);
        record.setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(record);
        return CommonResult.success(null);
    }

    // ===== 私有方法 =====

    private List<Long> getElderlyIdsByDoctor(Long doctorId) {
        List<Elderly> elderlyList = elderlyService.listByDoctorId(doctorId);
        return elderlyList.stream().map(Elderly::getId).collect(Collectors.toList());
    }
}
