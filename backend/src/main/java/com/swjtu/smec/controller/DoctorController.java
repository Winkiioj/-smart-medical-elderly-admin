package com.swjtu.smec.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swjtu.smec.common.config.UserContext;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.SysUser;
import com.swjtu.smec.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "医生管理", description = "社区管理员管理本社区医生")
@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 分页查询本社区医生列表
     */
    @Operation(summary = "医生列表（含签约数）")
    @GetMapping("/list")
    public CommonResult<Page<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {

        String community = UserContext.currentCommunity();
        if (community == null || community.isEmpty()) {
            return CommonResult.error(400, "当前用户无社区归属");
        }
        Page<Map<String, Object>> page = sysUserService.pageDoctors(community, pageNo, pageSize, keyword);
        return CommonResult.success(page, page.getTotal());
    }

    /**
     * 新增医生
     */
    @Operation(summary = "新增医生")
    @PostMapping("/add")
    public CommonResult<Map<String, String>> add(@RequestBody SysUser doctor) {
        String community = UserContext.currentCommunity();
        if (community == null || community.isEmpty()) {
            return CommonResult.error(400, "无社区归属，无法新增医生");
        }
        try {
            String initPwd = sysUserService.createDoctor(doctor, community);
            return CommonResult.success(Map.of("initPassword", initPwd));
        } catch (RuntimeException e) {
            return CommonResult.error(400, e.getMessage());
        }
    }

    /**
     * 编辑医生信息
     */
    @Operation(summary = "编辑医生信息")
    @PutMapping("/update")
    public CommonResult<?> update(@RequestBody SysUser doctor) {
        try {
            sysUserService.updateDoctor(doctor);
            return CommonResult.success(null);
        } catch (RuntimeException e) {
            return CommonResult.error(400, e.getMessage());
        }
    }

    /**
     * 启用/禁用医生
     */
    @Operation(summary = "启用/禁用医生")
    @PutMapping("/toggle-status")
    public CommonResult<?> toggleStatus(@RequestBody Map<String, Object> body) {
        Long doctorId = ((Number) body.get("doctorId")).longValue();
        Integer status = (Integer) body.get("status");
        try {
            sysUserService.toggleDoctorStatus(doctorId, status);
            return CommonResult.success(null);
        } catch (RuntimeException e) {
            return CommonResult.error(400, e.getMessage());
        }
    }

    /**
     * 下拉列表：本社区启用状态的医生（供老人分配用）
     */
    @Operation(summary = "医生下拉列表")
    @GetMapping("/options")
    public CommonResult<java.util.List<Map<String, Object>>> options() {
        String community = UserContext.currentCommunity();
        if (community == null || community.isEmpty()) {
            return CommonResult.error(400, "无社区归属");
        }
        // 简单返回 id + 姓名 + 签约数
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (SysUser u : sysUserService.listDoctorsByCommunity(community)) {
            Long count = sysUserService.countSigningElderly(u.getId());
            list.add(Map.of("id", u.getId(), "realName", u.getRealName(), "signingCount", count));
        }
        return CommonResult.success(list);
    }
}
