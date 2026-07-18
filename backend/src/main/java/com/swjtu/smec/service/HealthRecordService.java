package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.entity.HealthRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 健康数据 Service
 */
public interface HealthRecordService extends IService<HealthRecord> {

    /**
     * 查某老人在某时间段的健康数据（按日期降序）
     */
    List<HealthRecord> listByElderly(Long elderlyId, LocalDate start, LocalDate end);

    /**
     * 批量导入 JSON 数据，返回导入结果汇总
     */
    Map<String, Object> importJson(Long doctorId, String jsonContent);

    /**
     * [供 C 跨域调用] 写入一条健康数据（随访执行时同步）
     */
    int saveRecord(HealthRecord record);

    /**
     * [供 UC-DOC-04 / C 跨域调用] 获取某老人某指标的趋势数据
     *
     * @return [{"date": "2026-07-01", "value": 138}, ...]
     */
    List<Map<String, Object>> getTrend(Long elderlyId, String metricType,
                                       String startDate, String endDate);
}
