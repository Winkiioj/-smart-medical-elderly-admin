package com.swjtu.smec.controller;

import com.swjtu.smec.common.annotation.NoToken;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.AssessmentScore;
import com.swjtu.smec.service.AssessmentReportService;
import com.swjtu.smec.service.ElderlyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assessment")
public class AssessmentController {

    @Autowired
    private AssessmentReportService reportService;

    @Autowired
    private ElderlyService elderlyService;

    // TODO: A 完成登录认证后删除所有 @NoToken

    @NoToken
    @GetMapping("/elderly-list")
    public CommonResult elderlyList(@RequestParam Long doctorId) {
        return CommonResult.success(elderlyService.listByDoctorId(doctorId));
    }

    @NoToken
    @GetMapping("/template/list")
    public CommonResult templateList() {
        return reportService.listTemplates();
    }

    @NoToken
    @GetMapping("/report/page")
    public CommonResult page(@RequestParam(defaultValue = "1") int pageNo,
                             @RequestParam(defaultValue = "10") int pageSize,
                             @RequestParam Long doctorId) {
        return reportService.listByDoctor(pageNo, pageSize, doctorId);
    }

    @NoToken
    @PostMapping("/report/create")
    public CommonResult create(@RequestBody Map<String, Long> body) {
        return reportService.create(body.get("elderlyId"), body.get("templateId"), body.get("doctorId"));
    }

    @NoToken
    @PutMapping("/report/scores/{reportId}")
    public CommonResult saveScores(@PathVariable Long reportId,
                                   @RequestBody List<AssessmentScore> scores) {
        return reportService.saveScores(reportId, scores);
    }

    @NoToken
    @PostMapping("/report/complete/{reportId}")
    public CommonResult complete(@PathVariable Long reportId,
                                  @RequestBody Map<String, String> body) {
        return reportService.complete(reportId, body.get("conclusion"));
    }

    @NoToken
    @GetMapping("/report/detail/{id}")
    public CommonResult detail(@PathVariable Long id) {
        return reportService.getDetail(id);
    }
}
