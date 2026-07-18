package com.swjtu.smec.service;

import com.swjtu.smec.common.result.CommonResult;

/**
 * 报表统计 Service — A 负责
 * <p>
 * 跨域聚合查询 B/C 域数据，用于机构/社区管理员的报表展示。
 * 报表层使用 JdbcTemplate 做只读聚合查询，符合分层架构中"报表层"的设计惯例。
 *
 * @author A
 */
public interface ReportService {

    /**
     * 全局总览（UC-ORG-01）— 各社区对比数据
     */
    CommonResult overview();

    /**
     * 社区详情（UC-COM-03）— 单个社区深度下钻
     */
    CommonResult communityDetail(String community);

    /**
     * 管理员工作台 Dashboard — 全局聚合统计数据
     */
    CommonResult dashboard();

    /**
     * 导出全局总览 Excel（UC-ORG-02）— 返回 .xlsx 字节流
     */
    byte[] exportOverviewExcel();

    /**
     * 导出社区详情 Excel（UC-ORG-02）— 返回 .xlsx 字节流
     */
    byte[] exportCommunityDetailExcel(String community);
}
