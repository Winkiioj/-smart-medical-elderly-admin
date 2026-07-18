package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.AssessmentReport;

public interface AssessmentReportService extends IService<AssessmentReport> {
    CommonResult listTemplates();
    CommonResult listByDoctor(int pageNo, int pageSize, Long doctorId);
    CommonResult create(Long elderlyId, Long templateId, Long doctorId);
    CommonResult saveScores(Long reportId, java.util.List<com.swjtu.smec.entity.AssessmentScore> scores);
    CommonResult complete(Long reportId, String conclusion);
    CommonResult getDetail(Long id);
}
