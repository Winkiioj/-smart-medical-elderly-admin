package com.swjtu.smec.service.impl;

import com.swjtu.smec.common.config.UserContext;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.common.util.ExcelUtil;
import com.swjtu.smec.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * 报表统计 Service 实现 — A 负责
 * <p>
 * 报表层使用 JdbcTemplate 做只读聚合查询（跨域聚合场景的标准做法）。
 *
 * @author A
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ==================== 全局总览（UC-ORG-01） ====================

    @Override
    public CommonResult overview() {
        return CommonResult.success(buildOverviewData());
    }

    private Map<String, Object> buildOverviewData() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 1. 所有启用的社区列表
        List<String> communities = jdbcTemplate.queryForList(
                "SELECT DISTINCT community FROM elderly WHERE is_deleted = 0 AND community IS NOT NULL AND community != '' ORDER BY community",
                String.class);

        // 2. 各社区老人数
        List<Map<String, Object>> elderlyByCommunity = jdbcTemplate.queryForList(
                "SELECT community, COUNT(*) AS cnt FROM elderly WHERE is_deleted = 0 AND status = 1 GROUP BY community ORDER BY cnt DESC");

        // 3. 各社区医生数（联 sys_user_role 过滤 role_id=3）
        List<Map<String, Object>> doctorsByCommunity = jdbcTemplate.queryForList(
                "SELECT u.community, COUNT(DISTINCT u.id) AS cnt FROM sys_user u " +
                "INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.role_id = 3 " +
                "WHERE u.is_deleted = 0 AND u.status = 1 GROUP BY u.community ORDER BY cnt DESC");

        // 4. 各社区设备统计
        List<Map<String, Object>> devicesByCommunity = jdbcTemplate.queryForList(
                "SELECT community, " +
                "  COUNT(*) AS total, " +
                "  SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS online, " +
                "  SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS offline, " +
                "  SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) AS repairing " +
                "FROM device GROUP BY community ORDER BY total DESC");

        // 5. 各社区预警统计
        List<Map<String, Object>> warningsByCommunity = jdbcTemplate.queryForList(
                "SELECT e.community, " +
                "  COUNT(*) AS total, " +
                "  SUM(CASE WHEN wr.status = 0 THEN 1 ELSE 0 END) AS pending " +
                "FROM warning_record wr " +
                "INNER JOIN elderly e ON wr.elderly_id = e.id AND e.is_deleted = 0 " +
                "GROUP BY e.community ORDER BY total DESC");

        // 6. 全系统总计
        Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("elderly", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM elderly WHERE is_deleted = 0 AND status = 1", Long.class));
        totals.put("doctors", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user u INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
                "WHERE ur.role_id = 3 AND u.is_deleted = 0 AND u.status = 1", Long.class));
        totals.put("devices", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM device", Long.class));
        totals.put("warnings", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warning_record WHERE status IN (0, 1)", Long.class));

        data.put("communities", communities);
        data.put("elderlyByCommunity", elderlyByCommunity);
        data.put("doctorsByCommunity", doctorsByCommunity);
        data.put("devicesByCommunity", devicesByCommunity);
        data.put("warningsByCommunity", warningsByCommunity);
        data.put("totals", totals);

        return data;
    }

    // ==================== 社区详情（UC-COM-03） ====================

    @Override
    public CommonResult communityDetail(String community) {
        if (community == null || community.isEmpty()) {
            return CommonResult.error(400, "社区名称不能为空");
        }
        return CommonResult.success(buildCommunityDetailData(community));
    }

    private Map<String, Object> buildCommunityDetailData(String community) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("community", community);

        // 1. 老人统计
        Map<String, Object> elderlyStats = new LinkedHashMap<>();
        Long elderlyTotal = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM elderly WHERE community = ? AND is_deleted = 0 AND status = 1", Long.class, community);
        Long elderlyMale = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM elderly WHERE community = ? AND is_deleted = 0 AND status = 1 AND gender = 1", Long.class, community);
        Long elderlyFemale = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM elderly WHERE community = ? AND is_deleted = 0 AND status = 1 AND gender = 2", Long.class, community);
        Long elderlyAssigned = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM elderly WHERE community = ? AND is_deleted = 0 AND status = 1 AND doctor_id IS NOT NULL", Long.class, community);

        elderlyStats.put("total", elderlyTotal);
        elderlyStats.put("male", elderlyMale);
        elderlyStats.put("female", elderlyFemale);
        elderlyStats.put("assigned", elderlyAssigned);
        elderlyStats.put("unassigned", elderlyTotal != null && elderlyAssigned != null ? elderlyTotal - elderlyAssigned : 0);

        // 年龄分布
        List<Map<String, Object>> ageDist = jdbcTemplate.queryForList(
                "SELECT " +
                "  SUM(CASE WHEN age < 60 THEN 1 ELSE 0 END) AS 'lt60', " +
                "  SUM(CASE WHEN age >= 60 AND age < 70 THEN 1 ELSE 0 END) AS 's60', " +
                "  SUM(CASE WHEN age >= 70 AND age < 80 THEN 1 ELSE 0 END) AS 's70', " +
                "  SUM(CASE WHEN age >= 80 AND age < 90 THEN 1 ELSE 0 END) AS 's80', " +
                "  SUM(CASE WHEN age >= 90 THEN 1 ELSE 0 END) AS 's90' " +
                "FROM elderly WHERE community = ? AND is_deleted = 0 AND status = 1", community);
        elderlyStats.put("ageDistribution", ageDist.isEmpty() ? Map.of() : ageDist.get(0));

        data.put("elderly", elderlyStats);

        // 2. 医生工作量
        List<Map<String, Object>> doctorWorkload = jdbcTemplate.queryForList(
                "SELECT u.id, u.real_name, " +
                "  COALESCE(COUNT(e.id), 0) AS signing_count " +
                "FROM sys_user u " +
                "INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.role_id = 3 " +
                "LEFT JOIN elderly e ON e.doctor_id = u.id AND e.is_deleted = 0 AND e.status = 1 " +
                "WHERE u.community = ? AND u.is_deleted = 0 AND u.status = 1 " +
                "GROUP BY u.id, u.real_name ORDER BY signing_count DESC", community);
        data.put("doctorWorkload", doctorWorkload);

        // 3. 设备状态
        Map<String, Object> deviceStats = new LinkedHashMap<>();
        Long devTotal = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM device WHERE community = ?", Long.class, community);
        if (devTotal != null && devTotal > 0) {
            deviceStats.put("total", devTotal);
            deviceStats.put("online", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM device WHERE community = ? AND status = 1", Long.class, community));
            deviceStats.put("offline", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM device WHERE community = ? AND status = 2", Long.class, community));
            deviceStats.put("repairing", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM device WHERE community = ? AND status = 3", Long.class, community));
            deviceStats.put("scrapped", jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM device WHERE community = ? AND status = 4", Long.class, community));
        }
        data.put("devices", deviceStats);

        // 4. 预警统计
        Map<String, Object> warningStats = new LinkedHashMap<>();
        warningStats.put("pending", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warning_record wr " +
                "INNER JOIN elderly e ON wr.elderly_id = e.id AND e.is_deleted = 0 " +
                "WHERE e.community = ? AND wr.status = 0", Long.class, community));
        warningStats.put("processing", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warning_record wr " +
                "INNER JOIN elderly e ON wr.elderly_id = e.id AND e.is_deleted = 0 " +
                "WHERE e.community = ? AND wr.status = 1", Long.class, community));
        warningStats.put("completed", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warning_record wr " +
                "INNER JOIN elderly e ON wr.elderly_id = e.id AND e.is_deleted = 0 " +
                "WHERE e.community = ? AND wr.status = 2", Long.class, community));
        warningStats.put("total", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warning_record wr " +
                "INNER JOIN elderly e ON wr.elderly_id = e.id AND e.is_deleted = 0 " +
                "WHERE e.community = ?", Long.class, community));
        data.put("warnings", warningStats);

        // 5. 健康数据概览（最近30天均值）
        try {
            Map<String, Object> healthOverview = jdbcTemplate.queryForMap(
                    "SELECT " +
                    "  ROUND(AVG(hr.systolic_pressure), 1) AS avg_systolic, " +
                    "  ROUND(AVG(hr.diastolic_pressure), 1) AS avg_diastolic, " +
                    "  ROUND(AVG(hr.heart_rate), 1) AS avg_heart_rate, " +
                    "  ROUND(AVG(hr.blood_sugar), 1) AS avg_blood_sugar, " +
                    "  ROUND(AVG(hr.blood_oxygen), 1) AS avg_blood_oxygen, " +
                    "  COUNT(DISTINCT hr.elderly_id) AS measured_elderly " +
                    "FROM health_record hr " +
                    "INNER JOIN elderly e ON hr.elderly_id = e.id AND e.is_deleted = 0 " +
                    "WHERE e.community = ? AND hr.measure_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)",
                    community);
            data.put("healthOverview", healthOverview);
        } catch (Exception e) {
            data.put("healthOverview", Map.of("message", "暂无健康数据"));
        }

        // 6. 近30天新增老人趋势
        List<Map<String, Object>> admissionTrend = jdbcTemplate.queryForList(
                "SELECT admission_date AS date, COUNT(*) AS cnt " +
                "FROM elderly WHERE community = ? AND is_deleted = 0 " +
                "AND admission_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                "GROUP BY admission_date ORDER BY admission_date ASC", community);
        data.put("admissionTrend", admissionTrend);

        // 7. 近7天预警趋势
        List<Map<String, Object>> warningTrend = jdbcTemplate.queryForList(
                "SELECT DATE(wr.create_time) AS date, COUNT(*) AS cnt " +
                "FROM warning_record wr " +
                "INNER JOIN elderly e ON wr.elderly_id = e.id AND e.is_deleted = 0 " +
                "WHERE e.community = ? AND wr.create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
                "GROUP BY DATE(wr.create_time) ORDER BY date ASC", community);
        data.put("warningTrend", warningTrend);

        return data;
    }

    // ==================== 管理员 Dashboard（角色感知） ====================

    @Override
    public CommonResult dashboard() {
        String roleCode = UserContext.currentRoleCode();
        if ("COM_ADMIN".equals(roleCode)) {
            return CommonResult.success(buildComAdminDashboard(UserContext.currentCommunity()));
        }
        // ORG_ADMIN 或未登录默认走全局
        return CommonResult.success(buildOrgAdminDashboard());
    }

    /** 机构管理员Dashboard：全局概况 + 各社区对标 */
    private Map<String, Object> buildOrgAdminDashboard() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 卡片四件：社区数/老人总数/医生总数/设备在线率
        data.put("communityCount", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM community WHERE status = 1", Long.class));
        data.put("elderlyTotal", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM elderly WHERE is_deleted = 0 AND status = 1", Long.class));
        data.put("doctorTotal", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user u " +
                "INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.role_id = 3 " +
                "WHERE u.is_deleted = 0 AND u.status = 1", Long.class));
        Long deviceTotal = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM device", Long.class);
        Long deviceOnline = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM device WHERE status = 1", Long.class);
        data.put("deviceOnlineRate", (deviceTotal != null && deviceTotal > 0 && deviceOnline != null)
                ? deviceOnline * 100 / deviceTotal : 0);
        data.put("deviceTotal", deviceTotal != null ? deviceTotal : 0);

        // 各社区对标表（老人数 + 医生数 + 预警数）
        data.put("communityOverview", jdbcTemplate.queryForList(
                "SELECT c.name AS community, " +
                "  COALESCE(e.cnt, 0) AS elderly, " +
                "  COALESCE(d.cnt, 0) AS doctors, " +
                "  COALESCE(w.cnt, 0) AS warnings " +
                "FROM community c " +
                "LEFT JOIN (SELECT community, COUNT(*) AS cnt FROM elderly WHERE is_deleted = 0 AND status = 1 GROUP BY community) e ON c.name = e.community " +
                "LEFT JOIN (SELECT u.community, COUNT(*) AS cnt FROM sys_user u INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.role_id = 3 WHERE u.is_deleted = 0 AND u.status = 1 GROUP BY u.community) d ON c.name = d.community " +
                "LEFT JOIN (SELECT e2.community, COUNT(*) AS cnt FROM warning_record wr INNER JOIN elderly e2 ON wr.elderly_id = e2.id AND e2.is_deleted = 0 WHERE wr.status IN (0, 1) GROUP BY e2.community) w ON c.name = w.community " +
                "WHERE c.status = 1 ORDER BY c.id"));

        data.put("role", "ORG_ADMIN");
        return data;
    }

    /** 社区管理员Dashboard：本社区数据 */
    private Map<String, Object> buildComAdminDashboard(String community) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("community", community != null ? community : "");

        if (community == null || community.isEmpty()) {
            data.put("elderlyTotal", 0); data.put("newThisMonth", 0);
            data.put("doctorCount", 0); data.put("deviceOnlineRate", 0);
            data.put("role", "COM_ADMIN"); data.put("warning", "当前用户无社区归属");
            return data;
        }

        // 本社区老人总数
        data.put("elderlyTotal", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM elderly WHERE community = ? AND is_deleted = 0 AND status = 1",
                Long.class, community));

        // 本月新增老人
        data.put("newThisMonth", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM elderly WHERE community = ? AND is_deleted = 0 " +
                "AND admission_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01')",
                Long.class, community));

        // 本社区签约医生数
        data.put("doctorCount", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user u " +
                "INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.role_id = 3 " +
                "WHERE u.community = ? AND u.is_deleted = 0 AND u.status = 1",
                Long.class, community));

        // 设备在线率
        Long devTotal = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM device WHERE community = ?", Long.class, community);
        Long devOnline = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM device WHERE community = ? AND status = 1", Long.class, community);
        data.put("deviceOnlineRate", (devTotal != null && devTotal > 0 && devOnline != null)
                ? devOnline * 100 / devTotal : 0);

        // 待签约老人数（doctor_id IS NULL）
        data.put("unassignedElderly", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM elderly WHERE community = ? AND is_deleted = 0 AND status = 1 AND doctor_id IS NULL",
                Long.class, community));

        // 待处理预警（本社区）
        data.put("pendingWarnings", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM warning_record wr " +
                "INNER JOIN elderly e ON wr.elderly_id = e.id AND e.is_deleted = 0 " +
                "WHERE e.community = ? AND wr.status IN (0, 1)",
                Long.class, community));

        data.put("role", "COM_ADMIN");
        return data;
    }

    // ==================== Excel 导出（UC-ORG-02） ====================

    @Override
    public byte[] exportOverviewExcel() {
        try {
            return ExcelUtil.exportOverview(buildOverviewData());
        } catch (IOException e) {
            throw new RuntimeException("生成Excel失败：" + e.getMessage(), e);
        }
    }

    @Override
    public byte[] exportCommunityDetailExcel(String community) {
        try {
            return ExcelUtil.exportCommunityDetail(buildCommunityDetailData(community));
        } catch (IOException e) {
            throw new RuntimeException("生成Excel失败：" + e.getMessage(), e);
        }
    }
}
