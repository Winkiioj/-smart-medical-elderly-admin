package com.swjtu.smec.controller;

import com.swjtu.smec.common.config.UserContext;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * 报表统计 Controller — A 负责
 * <p>
 * UC-ORG-01: 全局总览 | UC-COM-03: 社区详情 | UC-ORG-02: 导出
 *
 * @author A
 */
@Tag(name = "报表统计", description = "机构/社区报表数据聚合")
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 管理员工作台 Dashboard — 全局聚合统计
     */
    @Operation(summary = "管理员工作台全局统计")
    @GetMapping("/dashboard")
    public CommonResult dashboard() {
        return reportService.dashboard();
    }

    /**
     * 全局总览 — 跨社区对比（机构管理员）
     */
    @Operation(summary = "全局总览（跨社区对比）")
    @GetMapping("/overview")
    public CommonResult overview() {
        return reportService.overview();
    }

    /**
     * 社区详情 — 单社区深度下钻
     * 社区管理员自动取本社区，机构管理员可传参查看任意社区
     */
    @Operation(summary = "社区详情（单社区下钻）")
    @GetMapping("/community-detail")
    public CommonResult communityDetail(
            @RequestParam(required = false) String community) {
        if (community == null || community.isEmpty()) {
            community = UserContext.currentCommunity();
            if (community == null || community.isEmpty()) {
                return CommonResult.error(400, "请指定社区或使用社区管理员账号登录");
            }
        }
        return reportService.communityDetail(community);
    }

    /**
     * 导出全局总览 Excel
     */
    @Operation(summary = "导出全局总览 Excel")
    @GetMapping("/export/overview")
    public void exportOverview(HttpServletResponse response) throws IOException {
        byte[] bytes = reportService.exportOverviewExcel();
        setExcelResponse(response, "全局总览报表_" + LocalDate.now() + ".xlsx");
        response.getOutputStream().write(bytes);
    }

    /**
     * 导出社区详情 Excel
     */
    @Operation(summary = "导出社区详情 Excel")
    @GetMapping("/export/community-detail")
    public void exportCommunityDetail(@RequestParam(required = false) String community,
                                      HttpServletResponse response) throws IOException {
        if (community == null || community.isEmpty()) {
            community = UserContext.currentCommunity();
        }
        byte[] bytes = reportService.exportCommunityDetailExcel(
                community != null ? community : "未知社区");
        setExcelResponse(response, "社区详情报表_" + (community != null ? community : "") + "_" + LocalDate.now() + ".xlsx");
        response.getOutputStream().write(bytes);
    }

    private void setExcelResponse(HttpServletResponse response, String filename) {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + encoded);
    }
}
