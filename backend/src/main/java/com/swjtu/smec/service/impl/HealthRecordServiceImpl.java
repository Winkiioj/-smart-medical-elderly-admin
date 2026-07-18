package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swjtu.smec.entity.Elderly;
import com.swjtu.smec.entity.HealthRecord;
import com.swjtu.smec.entity.ImportLog;
import com.swjtu.smec.entity.WarningRule;
import com.swjtu.smec.entity.WarningRecord;
import com.swjtu.smec.mapper.HealthRecordMapper;
import com.swjtu.smec.mapper.WarningRuleMapper;
import com.swjtu.smec.mapper.WarningRecordMapper;
import com.swjtu.smec.service.ElderlyService;
import com.swjtu.smec.service.HealthRecordService;
import com.swjtu.smec.service.ImportLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Transactional
public class HealthRecordServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord>
        implements HealthRecordService {

    @Autowired
    private ElderlyService elderlyService;

    @Autowired
    private ImportLogService importLogService;

    @Autowired
    private WarningRuleMapper warningRuleMapper;

    @Autowired
    private WarningRecordMapper warningRecordMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<HealthRecord> listByElderly(Long elderlyId, LocalDate start, LocalDate end) {
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getElderlyId, elderlyId);
        if (start != null) wrapper.ge(HealthRecord::getMeasureDate, start);
        if (end   != null) wrapper.le(HealthRecord::getMeasureDate, end);
        wrapper.orderByDesc(HealthRecord::getMeasureDate, HealthRecord::getMeasureTime);
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> importJson(Long doctorId, String jsonContent) {
        Map<String, Object> summary = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        List<HealthRecord> toInsert = new ArrayList<>();
        int warningCount = 0;
        String fileName = "import_" + System.currentTimeMillis() + ".json";

        // 1. 解析 JSON
        Map<String, Object> root;
        List<Map<String, Object>> records;
        try {
            root = objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
            Long fileDoctorId = ((Number) root.get("doctor_id")).longValue();
            if (!fileDoctorId.equals(doctorId)) {
                summary.put("success", false);
                summary.put("message", "文件中的 doctor_id 与当前登录用户不匹配");
                return summary;
            }
            records = (List<Map<String, Object>>) root.get("records");
            if (records == null || records.isEmpty()) {
                summary.put("success", false);
                summary.put("message", "文件中无数据记录");
                return summary;
            }
        } catch (Exception e) {
            summary.put("success", false);
            summary.put("message", "JSON 解析失败: " + e.getMessage());
            return summary;
        }

        // 2. 逐条校验
        int totalCount = records.size();
        for (int i = 0; i < records.size(); i++) {
            Map<String, Object> rec = records.get(i);
            try {
                HealthRecord hr = new HealthRecord();

                // elderly_id
                Long elderlyId = toLong(rec.get("elderly_id"));
                if (elderlyId == null) {
                    errors.add("第" + (i+1) + "条: 缺少 elderly_id"); continue;
                }
                Elderly elder = elderlyService.getById(elderlyId);
                if (elder == null || !elder.getDoctorId().equals(doctorId)) {
                    errors.add("第" + (i+1) + "条: 老人ID=" + elderlyId + " 不存在或不属于您的签约范围");
                    continue;
                }
                hr.setElderlyId(elderlyId);

                // measure_date
                String mdStr = (String) rec.get("measure_date");
                if (mdStr == null || mdStr.isEmpty()) {
                    errors.add("第" + (i+1) + "条: 缺少 measure_date"); continue;
                }
                LocalDate md = parseDate(mdStr);
                if (md == null) {
                    errors.add("第" + (i+1) + "条: 日期格式错误"); continue;
                }
                if (md.isAfter(LocalDate.now())) {
                    errors.add("第" + (i+1) + "条: 测量日期不能晚于今天"); continue;
                }
                hr.setMeasureDate(md);

                // measure_time
                String mtStr = (String) rec.get("measure_time");
                hr.setMeasureTime(mtStr != null ? parseTime(mtStr) : null);

                // 各指标
                Integer sbp = toInt(rec.get("systolic_pressure"));
                Integer dbp = toInt(rec.get("diastolic_pressure"));
                Integer hrt = toInt(rec.get("heart_rate"));
                BigDecimal bs = toDecimal(rec.get("blood_sugar"));
                Integer bo = toInt(rec.get("blood_oxygen"));
                BigDecimal w  = toDecimal(rec.get("weight"));
                BigDecimal h  = toDecimal(rec.get("height"));
                BigDecimal tp = toDecimal(rec.get("temperature"));

                // 至少一项指标
                if (sbp == null && dbp == null && hrt == null && bs == null
                        && bo == null && w == null && h == null && tp == null) {
                    errors.add("第" + (i+1) + "条: 至少需填写一项健康指标"); continue;
                }

                // 范围校验
                if (sbp != null && (sbp < 60 || sbp > 300)) {
                    errors.add("第" + (i+1) + "条: 收缩压 " + sbp + " 超出范围(60-300)"); continue;
                }
                if (dbp != null && (dbp < 30 || dbp > 200)) {
                    errors.add("第" + (i+1) + "条: 舒张压 " + dbp + " 超出范围(30-200)"); continue;
                }
                if (hrt != null && (hrt < 30 || hrt > 250)) {
                    errors.add("第" + (i+1) + "条: 心率 " + hrt + " 超出范围(30-250)"); continue;
                }

                hr.setSystolicPressure(sbp);
                hr.setDiastolicPressure(dbp);
                hr.setHeartRate(hrt);
                hr.setBloodSugar(bs);
                hr.setBloodOxygen(bo);
                hr.setWeight(w);
                hr.setHeight(h);
                hr.setTemperature(tp);

                // BMI = 体重/(身高/100)²
                if (w != null && h != null && h.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal hM = h.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                    hr.setBmi(w.divide(hM.multiply(hM), 1, RoundingMode.HALF_UP));
                }

                String remark = (String) rec.get("remark");
                hr.setRemark(remark);

                hr.setCreateBy(doctorId);
                hr.setCreateTime(LocalDateTime.now());

                // 重复检测：同一老人 + 同一日期 + 同一时间 → 跳过
                LambdaQueryWrapper<HealthRecord> dupWrapper = new LambdaQueryWrapper<>();
                dupWrapper.eq(HealthRecord::getElderlyId, elderlyId)
                          .eq(HealthRecord::getMeasureDate, md);
                if (hr.getMeasureTime() != null) {
                    dupWrapper.eq(HealthRecord::getMeasureTime, hr.getMeasureTime());
                }
                if (this.baseMapper.selectCount(dupWrapper) > 0) {
                    errors.add("第" + (i+1) + "条: 老人ID=" + elderlyId + " 在 " + mdStr
                            + (mtStr != null ? " " + mtStr : "")
                            + " 已有健康数据记录，重复导入已跳过");
                    continue;
                }

                toInsert.add(hr);

            } catch (Exception e) {
                errors.add("第" + (i+1) + "条: 数据处理异常 - " + e.getMessage());
            }
        }

        int successCount = toInsert.size();
        int failCount = totalCount - successCount;

        // 3. 批量写入
        if (!toInsert.isEmpty()) {
            this.saveBatch(toInsert, 500);
        }

        // 3.5 预警规则扫描
        // 查询所有启用的规则
        LambdaQueryWrapper<WarningRule> ruleWrapper = new LambdaQueryWrapper<>();
        ruleWrapper.eq(WarningRule::getIsEnabled, 1);
        List<WarningRule> rules = warningRuleMapper.selectList(ruleWrapper);

        if (!rules.isEmpty() && !toInsert.isEmpty()) {
            for (HealthRecord hr : toInsert) {
                Long elderlyId = hr.getElderlyId();
                LocalDate md = hr.getMeasureDate();

                for (WarningRule rule : rules) {
                    // 24h去重：同一老人+同一指标类型已有预警则跳过
                    Long dupCount = warningRecordMapper.selectCount(
                        new LambdaQueryWrapper<WarningRecord>()
                            .eq(WarningRecord::getElderlyId, elderlyId)
                            .eq(WarningRecord::getAlertType, rule.getIndicatorType())
                            .ge(WarningRecord::getCreateTime, LocalDateTime.now().minusHours(24))
                    );
                    if (dupCount > 0) continue;

                    // 阈值比对
                    boolean triggered = false;
                    BigDecimal maxVal = rule.getMaxValue();
                    BigDecimal minVal = rule.getMinValue();
                    BigDecimal actualVal = null;
                    String actualStr = null;

                    switch (rule.getIndicatorType()) {
                        case "systolic_pressure":
                            if (hr.getSystolicPressure() != null) {
                                actualVal = BigDecimal.valueOf(hr.getSystolicPressure());
                                actualStr = actualVal + "mmHg";
                                if (maxVal != null) triggered = actualVal.compareTo(maxVal) > 0;
                                if (minVal != null) triggered = triggered || actualVal.compareTo(minVal) < 0;
                            }
                            break;
                        case "diastolic_pressure":
                            if (hr.getDiastolicPressure() != null) {
                                actualVal = BigDecimal.valueOf(hr.getDiastolicPressure());
                                actualStr = actualVal + "mmHg";
                                if (maxVal != null) triggered = actualVal.compareTo(maxVal) > 0;
                                if (minVal != null) triggered = triggered || actualVal.compareTo(minVal) < 0;
                            }
                            break;
                        case "heart_rate":
                            if (hr.getHeartRate() != null) {
                                actualVal = BigDecimal.valueOf(hr.getHeartRate());
                                actualStr = actualVal + "bpm";
                                if (maxVal != null) triggered = actualVal.compareTo(maxVal) > 0;
                                if (minVal != null) triggered = triggered || actualVal.compareTo(minVal) < 0;
                            }
                            break;
                        case "blood_sugar":
                            if (hr.getBloodSugar() != null) {
                                actualVal = hr.getBloodSugar();
                                actualStr = actualVal + "mmol/L";
                                if (maxVal != null) triggered = actualVal.compareTo(maxVal) > 0;
                                if (minVal != null) triggered = triggered || actualVal.compareTo(minVal) < 0;
                            }
                            break;
                        case "blood_oxygen":
                            if (hr.getBloodOxygen() != null) {
                                actualVal = BigDecimal.valueOf(hr.getBloodOxygen());
                                actualStr = actualVal + "%";
                                // 血氧：低于阈值触发
                                if (minVal != null) triggered = actualVal.compareTo(minVal) < 0;
                            }
                            break;
                        case "bmi":
                            if (hr.getBmi() != null) {
                                actualVal = hr.getBmi();
                                actualStr = actualVal.toString();
                                if (maxVal != null) triggered = actualVal.compareTo(maxVal) > 0;
                            }
                            break;
                    }

                    if (triggered && actualStr != null) {
                        // 生成预警记录
                        WarningRecord wr = new WarningRecord();
                        wr.setElderlyId(elderlyId);
                        wr.setRuleId(rule.getId());
                        wr.setHealthRecordId(hr.getId());
                        wr.setAlertType(rule.getIndicatorType());
                        wr.setAlertLevel(rule.getAlertLevel());
                        wr.setAlertTitle(rule.getRuleName());
                        wr.setTriggerValue(actualStr);
                        String thres = maxVal != null ? ">" + maxVal : "<" + minVal;
                        wr.setThresholdValue(thres + rule.getIndicatorName());
                        wr.setStatus(0);
                        wr.setCreateTime(LocalDateTime.now());
                        warningRecordMapper.insert(wr);
                        warningCount++;
                    }
                }
            }
        }

        // 4. 写入导入日志
        ImportLog log = new ImportLog();
        log.setFileName(fileName);
        log.setDoctorId(doctorId);
        log.setTotalCount(totalCount);
        log.setSuccessCount(successCount);
        log.setFailCount(failCount);
        log.setWarningCount(warningCount);
        try {
            log.setFailDetail(objectMapper.writeValueAsString(errors));
        } catch (Exception ignored) {}
        importLogService.save(log);

        // 5. 汇总
        summary.put("success", true);
        summary.put("totalCount", totalCount);
        summary.put("successCount", successCount);
        summary.put("failCount", failCount);
        summary.put("warningCount", warningCount);
        summary.put("errors", errors);
        return summary;
    }

    // ===== 供 C 跨域调用的单条保存 =====
    @Override
    public int saveRecord(HealthRecord record) {
        // BMI 自动计算
        if (record.getWeight() != null && record.getHeight() != null
                && record.getHeight().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal hM = record.getHeight().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            record.setBmi(record.getWeight().divide(hM.multiply(hM), 1, RoundingMode.HALF_UP));
        }
        this.baseMapper.insert(record);
        return 1;
    }

    @Override
    public List<Map<String, Object>> getTrend(Long elderlyId, String metricType,
                                               String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end   = LocalDate.parse(endDate);
        LambdaQueryWrapper<HealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthRecord::getElderlyId, elderlyId);
        wrapper.between(HealthRecord::getMeasureDate, start, end);
        wrapper.isNotNull(getColumnByMetric(metricType));
        wrapper.orderByAsc(HealthRecord::getMeasureDate);
        List<HealthRecord> records = this.baseMapper.selectList(wrapper);

        List<Map<String, Object>> result = new ArrayList<>();
        for (HealthRecord r : records) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", r.getMeasureDate().toString());
            point.put("value", getValueByMetric(r, metricType));
            result.add(point);
        }
        return result;
    }

    // ===== 工具方法 =====
    private com.baomidou.mybatisplus.core.toolkit.support.SFunction<HealthRecord, ?>
            getColumnByMetric(String type) {
        return switch (type) {
            case "systolic_pressure"  -> HealthRecord::getSystolicPressure;
            case "diastolic_pressure" -> HealthRecord::getDiastolicPressure;
            case "heart_rate"         -> HealthRecord::getHeartRate;
            case "blood_sugar"        -> HealthRecord::getBloodSugar;
            case "blood_oxygen"       -> HealthRecord::getBloodOxygen;
            case "weight"             -> HealthRecord::getWeight;
            case "height"             -> HealthRecord::getHeight;
            case "bmi"                -> HealthRecord::getBmi;
            case "temperature"        -> HealthRecord::getTemperature;
            default -> throw new IllegalArgumentException("Unknown metric: " + type);
        };
    }

    private Object getValueByMetric(HealthRecord r, String type) {
        return switch (type) {
            case "systolic_pressure"  -> r.getSystolicPressure();
            case "diastolic_pressure" -> r.getDiastolicPressure();
            case "heart_rate"         -> r.getHeartRate();
            case "blood_sugar"        -> r.getBloodSugar();
            case "blood_oxygen"       -> r.getBloodOxygen();
            case "weight"             -> r.getWeight();
            case "height"             -> r.getHeight();
            case "bmi"                -> r.getBmi();
            case "temperature"        -> r.getTemperature();
            default -> null;
        };
    }

    private LocalDate parseDate(String s) { try { return LocalDate.parse(s); } catch (Exception e) { return null; } }
    private LocalTime parseTime(String s) { try { return LocalTime.parse(s); } catch (Exception e) { return null; } }
    private Integer toInt(Object v) { if (v == null) return null; try { return ((Number) v).intValue(); } catch (Exception e) { return null; } }
    private BigDecimal toDecimal(Object v) { if (v == null) return null; try { return new BigDecimal(v.toString()); } catch (Exception e) { return null; } }
    private Long toLong(Object v) { if (v == null) return null; try { return ((Number) v).longValue(); } catch (Exception e) { return null; } }
}
