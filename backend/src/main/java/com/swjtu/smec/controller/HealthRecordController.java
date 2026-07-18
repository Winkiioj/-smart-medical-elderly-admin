package com.swjtu.smec.controller;

import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.HealthRecord;
import com.swjtu.smec.service.HealthRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 健康数据 Controller — UC-DOC-03 + UC-DOC-04
 */
@Tag(name = "健康数据", description = "健康数据导入/查询/趋势图")
@RestController
@RequestMapping("/api/health-record")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    /**
     * 查某老人的健康数据列表
     */
    @Operation(summary = "查询老人健康数据")
    @GetMapping("/list/{elderlyId}")
    public CommonResult<List<HealthRecord>> list(
            @PathVariable Long elderlyId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end   = endDate   != null ? LocalDate.parse(endDate)   : null;
        return CommonResult.success(healthRecordService.listByElderly(elderlyId, start, end));
    }

    /**
     * 批量导入健康数据（JSON）— UC-DOC-03
     */
    @Operation(summary = "批量导入健康数据(JSON)")
    @PostMapping("/import")
    public CommonResult<Map<String, Object>> importData(@RequestBody Map<String, Object> body) {
        Long doctorId = ((Number) body.get("doctorId")).longValue();
        String jsonContent = (String) body.get("jsonContent");
        Map<String, Object> result = healthRecordService.importJson(doctorId, jsonContent);
        return CommonResult.success(result);
    }

    /**
     * 下载模板（医生专属）
     */
    @Operation(summary = "下载导入模板")
    @GetMapping("/template")
    public CommonResult<String> template(@RequestParam Long doctorId) {
        String template = """
        {
          "doctor_id": %d,
          "records": [
            {
              "elderly_id": 1,
              "measure_date": "2026-07-17",
              "measure_time": "08:30",
              "systolic_pressure": 138,
              "diastolic_pressure": 88,
              "heart_rate": 76,
              "blood_sugar": 6.2,
              "blood_oxygen": 97,
              "weight": 62.0,
              "height": 158.0,
              "temperature": 36.5,
              "remark": "饭后自述无不适"
            }
          ]
        }
        """.formatted(doctorId);
        return CommonResult.success(template);
    }

    /**
     * 趋势图数据 — UC-DOC-04
     */
    @Operation(summary = "获取健康指标趋势图数据")
    @GetMapping("/trend")
    public CommonResult<List<Map<String, Object>>> trend(
            @RequestParam Long elderlyId,
            @RequestParam String metricType,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return CommonResult.success(
                healthRecordService.getTrend(elderlyId, metricType, startDate, endDate));
    }
}
