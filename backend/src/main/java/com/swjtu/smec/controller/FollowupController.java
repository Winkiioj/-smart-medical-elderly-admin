package com.swjtu.smec.controller;

import com.swjtu.smec.common.annotation.NoToken;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.FollowupPlan;
import com.swjtu.smec.entity.FollowupRecord;
import com.swjtu.smec.service.ElderlyService;
import com.swjtu.smec.service.FollowupPlanService;
import com.swjtu.smec.service.FollowupRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/followup")
public class FollowupController {

    @Autowired
    private FollowupPlanService planService;

    @Autowired
    private FollowupRecordService recordService;

    @Autowired
    private ElderlyService elderlyService;

    @NoToken
    @GetMapping("/elderly-list")
    public CommonResult elderlyList(@RequestParam Long doctorId) {
        return CommonResult.success(elderlyService.listByDoctorId(doctorId));
    }

    // TODO: A 完成登录认证后删除所有 @NoToken

    @NoToken
    @GetMapping("/plan/{id}")
    public CommonResult planDetail(@PathVariable Long id) {
        return planService.getPlanById(id);
    }

    @NoToken
    @GetMapping("/plan/page")
    public CommonResult page(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Integer followupType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return planService.pageByDoctor(pageNo, pageSize, doctorId, followupType, status, startDate, endDate);
    }

    @NoToken
    @PostMapping("/plan/create")
    public CommonResult create(@RequestBody FollowupPlan plan) {
        return planService.create(plan);
    }

    @NoToken
    @PostMapping("/plan/start/{id}")
    public CommonResult start(@PathVariable Long id, @RequestParam Long doctorId) {
        return planService.start(id, doctorId);
    }

    @NoToken
    @PostMapping("/plan/complete/{id}")
    public CommonResult complete(@PathVariable Long id, @RequestBody FollowupRecord record) {
        return planService.complete(id, record);
    }

    @NoToken
    @PutMapping("/plan/date/{id}")
    public CommonResult updateDate(@PathVariable Long id, @RequestParam String planDate) {
        return planService.updatePlanDate(id, java.time.LocalDate.parse(planDate));
    }

    @NoToken
    @GetMapping("/record/recent/{elderlyId}")
    public CommonResult recentRecords(@PathVariable Long elderlyId) {
        return recordService.getRecentByElderly(elderlyId, 3);
    }

    @NoToken
    @GetMapping("/record/by-plan/{planId}")
    public CommonResult recordByPlan(@PathVariable Long planId) {
        return recordService.getByPlanId(planId);
    }
}
