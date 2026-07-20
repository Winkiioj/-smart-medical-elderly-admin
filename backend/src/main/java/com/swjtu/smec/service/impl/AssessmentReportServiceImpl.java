package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.*;
import com.swjtu.smec.mapper.*;
import com.swjtu.smec.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class AssessmentReportServiceImpl
        extends ServiceImpl<AssessmentReportMapper, AssessmentReport>
        implements AssessmentReportService {

    @Autowired private AssessmentTemplateMapper templateMapper;
    @Autowired private AssessmentDimensionMapper dimensionMapper;
    @Autowired private AssessmentScoreMapper scoreMapper;
    @Autowired private ElderlyService elderlyService;
    @Autowired private FollowupRecordService followupRecordService;

    @Override
    public CommonResult listTemplates() {
        return CommonResult.success(templateMapper.selectList(null));
    }

    @Override
    public CommonResult listByDoctor(int pageNo, int pageSize, Long doctorId) {
        LambdaQueryWrapper<AssessmentReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssessmentReport::getDoctorId, doctorId)
               .orderByDesc(AssessmentReport::getCreateTime);
        Page<AssessmentReport> page = new Page<>(pageNo, pageSize);
        IPage<AssessmentReport> result = this.baseMapper.selectPage(page, wrapper);
        // 填充老人姓名和模板名称
        for (AssessmentReport r : result.getRecords()) {
            Elderly e = elderlyService.getById(r.getElderlyId());
            if (e != null) r.setElderlyName(e.getName());
            AssessmentTemplate t = templateMapper.selectById(r.getTemplateId());
            if (t != null) r.setTemplateName(t.getTemplateName());
        }
        return CommonResult.success(result.getRecords(), result.getTotal());
    }

    @Override
    public CommonResult create(Long elderlyId, Long templateId, Long doctorId) {
        // 一月去重
        AssessmentReport exist = this.baseMapper.selectLatestPublished(elderlyId, templateId);
        if (exist != null) {
            return CommonResult.error(400, "该老人本月已存在同模板评估报告，请使用修订版本");
        }
        AssessmentReport report = new AssessmentReport();
        report.setElderlyId(elderlyId);
        report.setTemplateId(templateId);
        report.setDoctorId(doctorId);
        report.setStatus(0);
        report.setCreateTime(LocalDateTime.now());
        this.baseMapper.insert(report);

        // 拉取老人信息 + 最近随访摘要
        Elderly elderly = elderlyService.getById(elderlyId);
        CommonResult recentResult = followupRecordService.getRecentByElderly(elderlyId, 3);
        List<?> recentFollowups = (List<?>) recentResult.getData();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("report", report);
        data.put("elderly", elderly);
        data.put("recentFollowups", recentFollowups);
        return CommonResult.success(data);
    }

    @Override
    public CommonResult saveScores(Long reportId, List<AssessmentScore> scores) {
        AssessmentReport report = this.baseMapper.selectById(reportId);
        if (report == null) return CommonResult.error(404, "报告不存在");
        if (report.getStatus() != 0) return CommonResult.error(400, "仅草稿状态可保存评分");
        // 先删后插
        scoreMapper.deleteByReportId(reportId);
        for (AssessmentScore s : scores) {
            s.setReportId(reportId);
            scoreMapper.insert(s);
        }
        return CommonResult.success(null);
    }

    @Override
    public CommonResult complete(Long reportId, String conclusion) {
        AssessmentReport report = this.baseMapper.selectById(reportId);
        if (report == null) return CommonResult.error(404, "报告不存在");
        if (report.getStatus() != 0) return CommonResult.error(400, "仅草稿状态可完成");
        if (conclusion == null || conclusion.length() < 20)
            return CommonResult.error(400, "评估结论不少于20字");

        // 拉取维度清单和评分
        List<AssessmentDimension> dims = dimensionMapper.selectByTemplateId(report.getTemplateId());
        List<AssessmentScore> scores = scoreMapper.selectByReportId(reportId);

        // 计算加权总分
        BigDecimal total = BigDecimal.ZERO;
        int full = 0;
        for (AssessmentDimension d : dims) {
            int score = 0;
            for (AssessmentScore s : scores) {
                if (s.getDimensionId().equals(d.getId())) { score = s.getScore(); break; }
            }
            total = total.add(BigDecimal.valueOf(score).multiply(d.getWeight()));
            full += d.getMaxScore();
        }
        report.setTotalScore(total.setScale(1, RoundingMode.HALF_UP));
        report.setFullScore(full);

        // 判定等级（防除零）
        if (full == 0) return CommonResult.error(400, "该模板未配置评分维度，请联系管理员");
        BigDecimal pct = total.divide(BigDecimal.valueOf(full), 4, RoundingMode.HALF_UP);
        if (pct.compareTo(BigDecimal.valueOf(0.9)) >= 0) report.setScoreLevel("优秀");
        else if (pct.compareTo(BigDecimal.valueOf(0.75)) >= 0) report.setScoreLevel("良好");
        else if (pct.compareTo(BigDecimal.valueOf(0.6)) >= 0) report.setScoreLevel("一般");
        else report.setScoreLevel("较差");

        report.setSuggestion(conclusion);
        report.setStatus(1);
        report.setUpdateTime(LocalDateTime.now());
        this.baseMapper.updateById(report);

        return CommonResult.success(report);
    }

    @Override
    public CommonResult getDetail(Long id) {
        AssessmentReport report = this.baseMapper.selectById(id);
        if (report == null) return CommonResult.error(404, "报告不存在");
        List<AssessmentScore> scores = scoreMapper.selectByReportId(id);
        List<AssessmentDimension> dims = dimensionMapper.selectByTemplateId(report.getTemplateId());
        Elderly elderly = elderlyService.getById(report.getElderlyId());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("report", report);
        data.put("scores", scores);
        data.put("dimensions", dims);
        data.put("elderly", elderly);
        return CommonResult.success(data);
    }
}
