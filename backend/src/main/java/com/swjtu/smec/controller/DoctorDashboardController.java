package com.swjtu.smec.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Elderly;
import com.swjtu.smec.service.DeviceService;
import com.swjtu.smec.service.ElderlyService;
import com.swjtu.smec.service.WarningRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Tag(name = "医生工作台", description = "医生 Dashboard 聚合数据")
@RestController
@RequestMapping("/api/doctor")
public class DoctorDashboardController {

    @Autowired
    private ElderlyService elderlyService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private WarningRecordService warningRecordService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/dashboard")
    public CommonResult<Map<String, Object>> dashboard(@RequestParam Long doctorId) {
        Map<String, Object> data = new LinkedHashMap<>();

        // 1. 负责老人总数
        long totalElderly = elderlyService.lambdaQuery()
                .eq(Elderly::getDoctorId, doctorId)
                .eq(Elderly::getStatus, 1)
                .count();
        data.put("totalElderly", (int) totalElderly);

        // 2. 本月新增
        LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
        long newThisMonth = elderlyService.lambdaQuery()
                .eq(Elderly::getDoctorId, doctorId)
                .ge(Elderly::getAdmissionDate, firstOfMonth)
                .count();
        data.put("newThisMonth", (int) newThisMonth);

        // 3. 设备在线率 —— 跨域调用 C 的 DeviceService
        try {
            com.swjtu.smec.common.result.CommonResult statsResult = deviceService.getStats();
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) statsResult.getData();
            if (stats != null) {
                int total = (int) stats.get("total");
                int online = (int) stats.get("online");
                data.put("deviceOnlineRate", total > 0 ? (online * 100 / total) + "%" : "——");
            } else {
                data.put("deviceOnlineRate", "——");
            }
        } catch (Exception ignored) {
            data.put("deviceOnlineRate", "——");
        }

        // 4. 待处理预警 —— 跨域调用 C 的 WarningRecordService
        try {
            data.put("pendingWarnings", warningRecordService.countPendingByDoctorId(doctorId));
        } catch (Exception e) {
            data.put("pendingWarnings", 0);
        }

        // 5. 未读消息 —— TODO: A 创建 NotificationService 后替换为跨域 Service 调用
        try {
            Integer unread = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notification WHERE user_id = ? AND is_read = 0",
                Integer.class, doctorId);
            data.put("unreadMessages", unread != null ? unread : 0);
        } catch (Exception e) {
            data.put("unreadMessages", 0);
        }

        // 6. 年龄分布
        List<Elderly> all = elderlyService.lambdaQuery()
                .eq(Elderly::getDoctorId, doctorId)
                .eq(Elderly::getStatus, 1).list();
        int a60 = 0, a70 = 0, a80 = 0, a90 = 0;
        for (Elderly e : all) {
            if (e.getAge() == null) continue;
            if (e.getAge() >= 90) a90++;
            else if (e.getAge() >= 80) a80++;
            else if (e.getAge() >= 70) a70++;
            else if (e.getAge() >= 60) a60++;
        }
        data.put("ageDistribution", Map.of("60-69", a60, "70-79", a70, "80-89", a80, "90+", a90));

        // 7. 近30天新增趋势
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long cnt = elderlyService.lambdaQuery()
                    .eq(Elderly::getDoctorId, doctorId)
                    .eq(Elderly::getAdmissionDate, date).count();
            trend.add(Map.of("date", date.toString(), "count", (int) cnt));
        }
        data.put("admissionTrend", trend);

        return CommonResult.success(data);
    }
}
