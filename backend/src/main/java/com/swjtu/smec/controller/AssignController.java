package com.swjtu.smec.controller;

import com.swjtu.smec.common.config.UserContext;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.service.AssignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "老人分配", description = "社区管理员为新入住老人分配签约医生")
@RestController
@RequestMapping("/api/assign")
public class AssignController {

    @Autowired
    private AssignService assignService;

    private String getCommunity() {
        String c = UserContext.currentCommunity();
        if (c == null || c.isEmpty()) throw new RuntimeException("无社区归属");
        return c;
    }

    @Operation(summary = "待分配老人列表")
    @GetMapping("/unassigned")
    public CommonResult<Map<String, Object>> unassigned(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            Map<String, Object> result = assignService.listUnassigned(getCommunity(), pageNo, pageSize, keyword);
            return CommonResult.success(result, (Long) result.get("total"));
        } catch (RuntimeException e) {
            return CommonResult.error(400, e.getMessage());
        }
    }

    @Operation(summary = "已分配老人列表")
    @GetMapping("/assigned")
    public CommonResult<Map<String, Object>> assigned(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) String keyword) {
        try {
            Map<String, Object> result = assignService.listAssigned(getCommunity(), pageNo, pageSize, doctorId, keyword);
            return CommonResult.success(result, (Long) result.get("total"));
        } catch (RuntimeException e) {
            return CommonResult.error(400, e.getMessage());
        }
    }

    @Operation(summary = "医生负荷")
    @GetMapping("/doctor-load")
    public CommonResult<List<Map<String, Object>>> doctorLoad() {
        try {
            List<Map<String, Object>> list = assignService.getDoctorLoad(getCommunity());
            return CommonResult.success(list);
        } catch (RuntimeException e) {
            return CommonResult.error(400, e.getMessage());
        }
    }

    @Operation(summary = "分配医生")
    @PutMapping("/assign")
    public CommonResult<?> assign(@RequestBody Map<String, Object> body) {
        Long elderlyId = ((Number) body.get("elderlyId")).longValue();
        Long doctorId = ((Number) body.get("doctorId")).longValue();
        try {
            assignService.assignDoctor(elderlyId, doctorId);
            return CommonResult.success(null);
        } catch (RuntimeException e) {
            return CommonResult.error(400, e.getMessage());
        }
    }

    @Operation(summary = "批量分配")
    @PutMapping("/batch-assign")
    public CommonResult<Map<String, Integer>> batchAssign(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idsRaw = (List<Integer>) body.get("elderlyIds");
        List<Long> elderlyIds = idsRaw.stream().map(Integer::longValue).toList();
        Long doctorId = ((Number) body.get("doctorId")).longValue();
        try {
            int count = assignService.batchAssign(elderlyIds, doctorId);
            return CommonResult.success(Map.of("assigned", count));
        } catch (RuntimeException e) {
            return CommonResult.error(400, e.getMessage());
        }
    }

    @Operation(summary = "调整分配")
    @PutMapping("/reassign")
    public CommonResult<?> reassign(@RequestBody Map<String, Object> body) {
        Long elderlyId = ((Number) body.get("elderlyId")).longValue();
        Long newDoctorId = ((Number) body.get("newDoctorId")).longValue();
        try {
            assignService.reassignDoctor(elderlyId, newDoctorId);
            return CommonResult.success(null);
        } catch (RuntimeException e) {
            return CommonResult.error(400, e.getMessage());
        }
    }
}
