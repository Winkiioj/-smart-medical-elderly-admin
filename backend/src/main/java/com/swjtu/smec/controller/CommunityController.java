package com.swjtu.smec.controller;

import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Community;
import com.swjtu.smec.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 社区管理 Controller — A 负责（仅机构管理员可操作）
 *
 * @author A
 */
@Tag(name = "社区管理", description = "机构管理员增删改社区")
@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Operation(summary = "社区列表（分页）")
    @GetMapping("/page")
    public CommonResult page(@RequestParam(defaultValue = "1") int pageNo,
                             @RequestParam(defaultValue = "20") int pageSize,
                             @RequestParam(required = false) String keyword) {
        return communityService.page(pageNo, pageSize, keyword);
    }

    @Operation(summary = "启用社区列表（下拉用）")
    @GetMapping("/enabled")
    public CommonResult listEnabled() {
        return communityService.listEnabled();
    }

    @Operation(summary = "新增社区")
    @PostMapping
    public CommonResult add(@RequestBody Community community) {
        return communityService.add(community);
    }

    @Operation(summary = "编辑社区")
    @PutMapping
    public CommonResult update(@RequestBody Community community) {
        return communityService.update(community);
    }

    @Operation(summary = "停用/启用社区")
    @PutMapping("/toggle-status")
    public CommonResult toggleStatus(@RequestBody Map<String, Object> body) {
        Long id = ((Number) body.get("id")).longValue();
        Integer status = (Integer) body.get("status");
        return communityService.toggleStatus(id, status);
    }
}
