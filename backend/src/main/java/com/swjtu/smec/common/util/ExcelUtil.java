package com.swjtu.smec.common.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Excel .xlsx 生成工具
 *
 * @author A
 */
public class ExcelUtil {

    /** 标题样式 */
    private static CellStyle titleStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 16);
        s.setFont(f); s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    /** 表头样式 */
    private static CellStyle headerStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true); f.setFontHeightInPoints((short) 11); f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex()); s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        s.setAlignment(HorizontalAlignment.CENTER); s.setVerticalAlignment(VerticalAlignment.CENTER);
        s.setBorderBottom(BorderStyle.THIN); s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN); s.setBorderRight(BorderStyle.THIN);
        return s;
    }

    /** 数据行样式 */
    private static CellStyle dataStyle(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setBorderBottom(BorderStyle.THIN); s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN); s.setBorderRight(BorderStyle.THIN);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private static void fillRow(Sheet sheet, int rowNum, CellStyle style, String... values) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            Cell c = row.createCell(i);
            c.setCellValue(values[i] != null ? values[i] : "");
            c.setCellStyle(style);
        }
    }

    /**
     * 智能列宽：遍历所有行计算最大字符宽度。
     * 中文/全角字符算 2 个单位，英文/数字算 1 个单位，最后加 padding。
     * POI 列宽单位：1/256 个字符宽度。
     */
    private static void fitColumns(Sheet sheet, int colCount, int minChars, int paddingChars) {
        for (int col = 0; col < colCount; col++) {
            int maxWidth = minChars;
            for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Cell cell = row.getCell(col);
                if (cell == null) continue;
                String val;
                try {
                    val = cell.getStringCellValue();
                } catch (Exception e) {
                    val = cell.toString();
                }
                if (val == null || val.isEmpty()) continue;
                int w = 0;
                for (char c : val.toCharArray()) {
                    // CJK 统一表意文字 + 全角符号/标点
                    w += (c >= 0x2000) ? 2 : 1;
                }
                maxWidth = Math.max(maxWidth, w);
            }
            sheet.setColumnWidth(col, (maxWidth + paddingChars) * 256);
        }
    }

    // ==================== 全局总览 ====================

    @SuppressWarnings("unchecked")
    public static byte[] exportOverview(Map<String, Object> data) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // --- Sheet 1: 社区对比 ---
            Sheet s1 = wb.createSheet("社区对比");
            s1.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
            fillRow(s1, 0, titleStyle(wb), "智慧医养管理系统 — 社区对比报表");

            String[] headers = {"社区", "在院老人", "签约医生", "设备总数", "在线设备", "预警总数"};
            fillRow(s1, 2, headerStyle(wb), headers);

            List<Map<String, Object>> elderlyByCom = (List<Map<String, Object>>) data.get("elderlyByCommunity");
            List<Map<String, Object>> doctorsByCom = (List<Map<String, Object>>) data.get("doctorsByCommunity");
            List<Map<String, Object>> devicesByCom = (List<Map<String, Object>>) data.get("devicesByCommunity");
            List<Map<String, Object>> warningsByCom = (List<Map<String, Object>>) data.get("warningsByCommunity");

            int row = 3;
            for (Map<String, Object> ec : elderlyByCom != null ? elderlyByCom : List.<Map<String, Object>>of()) {
                String community = (String) ec.get("community");
                String elderly = String.valueOf(ec.get("cnt"));
                String doctors = findCnt(doctorsByCom, community);
                String devices = findTotal(devicesByCom, community);
                String online = findOnline(devicesByCom, community);
                String warnings = findCnt(warningsByCom, community);
                fillRow(s1, row++, dataStyle(wb), community, elderly, doctors, devices, online, warnings);
            }

            // 总计行
            Map<String, Object> totals = (Map<String, Object>) data.get("totals");
            if (totals != null) {
                fillRow(s1, row, headerStyle(wb),
                        "合计",
                        String.valueOf(totals.getOrDefault("elderly", 0)),
                        String.valueOf(totals.getOrDefault("doctors", 0)),
                        String.valueOf(totals.getOrDefault("devices", 0)),
                        "-",
                        String.valueOf(totals.getOrDefault("warnings", 0)));
            }

            fitColumns(s1, headers.length, 8, 4);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ==================== 社区详情 ====================

    @SuppressWarnings("unchecked")
    public static byte[] exportCommunityDetail(Map<String, Object> data) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            String community = (String) data.get("community");
            String prefix = (community != null ? community : "") + " — ";

            // ========== Sheet 1: 社区概览 ==========
            Sheet s1 = wb.createSheet("社区概览");
            s1.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
            fillRow(s1, 0, titleStyle(wb), prefix + "社区详情报表");

            int r = 2;
            Map<String, Object> elderly = (Map<String, Object>) data.get("elderly");
            Map<String, Object> devices = (Map<String, Object>) data.get("devices");
            Map<String, Object> warnings = (Map<String, Object>) data.get("warnings");

            fillRow(s1, r++, headerStyle(wb), "指标", "数值", "备注");
            fillRow(s1, r++, dataStyle(wb), "在院老人", str(elderly, "total"), "含已签约" + str(elderly, "assigned") + "人");
            fillRow(s1, r++, dataStyle(wb), "签约医生", String.valueOf(((List<?>) data.getOrDefault("doctorWorkload", List.of())).size()), "");
            fillRow(s1, r++, dataStyle(wb), "在线设备", str(devices, "online"), "共" + str(devices, "total") + "台");
            fillRow(s1, r++, dataStyle(wb), "待处理预警", str(warnings, "pending"), "共" + str(warnings, "total") + "条");
            fillRow(s1, r++, dataStyle(wb), "男", str(elderly, "male"), "");
            fillRow(s1, r++, dataStyle(wb), "女", str(elderly, "female"), "");
            fitColumns(s1, 3, 8, 4);

            // ========== Sheet 2: 年龄分布（对应前端饼图） ==========
            Sheet s2 = wb.createSheet("年龄分布");
            fillRow(s2, 0, titleStyle(wb), prefix + "年龄分布");
            fillRow(s2, 2, headerStyle(wb), "年龄段", "人数", "占比");
            Map<String, Object> ageDist = (Map<String, Object>)
                    (elderly != null ? elderly.get("ageDistribution") : null);
            int r2 = 3;
            long ageTotal = 0;
            if (ageDist != null) {
                String[][] ageData = {
                        {"<60岁", "lt60"}, {"60-69岁", "s60"}, {"70-79岁", "s70"},
                        {"80-89岁", "s80"}, {"≥90岁", "s90"}
                };
                for (String[] ad : ageData) {
                    ageTotal += toLong(ageDist.get(ad[1]));
                }
                for (String[] ad : ageData) {
                    long v = toLong(ageDist.get(ad[1]));
                    String pct = ageTotal > 0 ? String.format("%.1f%%", v * 100.0 / ageTotal) : "0.0%";
                    fillRow(s2, r2++, dataStyle(wb), ad[0], String.valueOf(v), pct);
                }
                fillRow(s2, r2, headerStyle(wb), "合计", String.valueOf(ageTotal), "100.0%");
            } else {
                fillRow(s2, r2, dataStyle(wb), "暂无数据", "", "");
            }
            fitColumns(s2, 3, 8, 3);

            // ========== Sheet 3: 医生工作量（对应前端横向柱状图） ==========
            Sheet s3 = wb.createSheet("医生工作量");
            fillRow(s3, 0, titleStyle(wb), prefix + "医生工作量");
            fillRow(s3, 2, headerStyle(wb), "医生姓名", "签约老人数", "占比");
            List<Map<String, Object>> workload = (List<Map<String, Object>>) data.get("doctorWorkload");
            int r3 = 3;
            if (workload != null && !workload.isEmpty()) {
                long workTotal = 0;
                for (Map<String, Object> d : workload) {
                    workTotal += toLong(d.get("signing_count"));
                }
                for (Map<String, Object> d : workload) {
                    long cnt = toLong(d.get("signing_count"));
                    String pct = workTotal > 0 ? String.format("%.1f%%", cnt * 100.0 / workTotal) : "0.0%";
                    fillRow(s3, r3++, dataStyle(wb),
                            String.valueOf(d.getOrDefault("real_name", "")),
                            String.valueOf(cnt), pct);
                }
                fillRow(s3, r3, headerStyle(wb), "合计", String.valueOf(workTotal), "100.0%");
            } else {
                fillRow(s3, r3, dataStyle(wb), "暂无数据", "", "");
            }
            fitColumns(s3, 3, 8, 4);

            // ========== Sheet 4: 设备状态（对应前端设备状态描述列表） ==========
            Sheet s4 = wb.createSheet("设备状态");
            fillRow(s4, 0, titleStyle(wb), prefix + "设备状态明细");
            fillRow(s4, 2, headerStyle(wb), "状态", "数量", "占比");
            int r4 = 3;
            if (devices != null && toLong(devices.get("total")) > 0) {
                long devTotal = toLong(devices.get("total"));
                String[][] devStatus = {
                        {"在线", "online"}, {"离线", "offline"}, {"维修中", "repairing"}, {"已报废", "scrapped"}
                };
                for (String[] ds : devStatus) {
                    long v = toLong(devices.get(ds[1]));
                    String pct = String.format("%.1f%%", v * 100.0 / devTotal);
                    fillRow(s4, r4++, dataStyle(wb), ds[0], String.valueOf(v), pct);
                }
                fillRow(s4, r4, headerStyle(wb), "合计", String.valueOf(devTotal), "100.0%");
            } else {
                fillRow(s4, r4, dataStyle(wb), "暂无数据", "", "");
            }
            fitColumns(s4, 3, 8, 3);

            // ========== Sheet 5: 30天健康概览（对应前端健康概览描述列表） ==========
            Sheet s5 = wb.createSheet("30天健康概览");
            fillRow(s5, 0, titleStyle(wb), prefix + "近30天健康数据概览");
            fillRow(s5, 2, headerStyle(wb), "指标", "数值", "说明");
            Map<String, Object> health = (Map<String, Object>) data.get("healthOverview");
            int r5 = 3;
            if (health != null && health.containsKey("avg_systolic")) {
                fillRow(s5, r5++, dataStyle(wb), "平均收缩压", str(health, "avg_systolic") + " mmHg", "");
                fillRow(s5, r5++, dataStyle(wb), "平均舒张压", str(health, "avg_diastolic") + " mmHg", "");
                fillRow(s5, r5++, dataStyle(wb), "平均心率", str(health, "avg_heart_rate") + " bpm", "");
                fillRow(s5, r5++, dataStyle(wb), "平均血糖", str(health, "avg_blood_sugar") + " mmol/L", "");
                fillRow(s5, r5++, dataStyle(wb), "平均血氧", str(health, "avg_blood_oxygen") + " %", "");
                fillRow(s5, r5++, dataStyle(wb), "已测量老人", str(health, "measured_elderly") + " 人", "");
            } else {
                fillRow(s5, r5, dataStyle(wb), "暂无数据", "", "");
            }
            fitColumns(s5, 3, 8, 3);

            // ========== Sheet 6: 近30天新增趋势（对应前端折线图） ==========
            Sheet s6 = wb.createSheet("30天新增趋势");
            fillRow(s6, 0, titleStyle(wb), prefix + "近30天新增老人趋势");
            fillRow(s6, 2, headerStyle(wb), "日期", "新增人数");
            List<Map<String, Object>> trend = (List<Map<String, Object>>) data.get("admissionTrend");
            int r6 = 3;
            if (trend != null && !trend.isEmpty()) {
                for (Map<String, Object> t : trend) {
                    fillRow(s6, r6++, dataStyle(wb),
                            String.valueOf(t.getOrDefault("date", "")),
                            String.valueOf(t.getOrDefault("cnt", 0)));
                }
            } else {
                fillRow(s6, r6, dataStyle(wb), "暂无数据", "");
            }
            fitColumns(s6, 2, 10, 3);

            // ========== Sheet 7: 近7天预警趋势（对应前端折线图） ==========
            Sheet s7 = wb.createSheet("7天预警趋势");
            fillRow(s7, 0, titleStyle(wb), prefix + "近7天预警趋势");
            fillRow(s7, 2, headerStyle(wb), "日期", "预警数");
            List<Map<String, Object>> warningTrend = (List<Map<String, Object>>) data.get("warningTrend");
            int r7 = 3;
            if (warningTrend != null && !warningTrend.isEmpty()) {
                for (Map<String, Object> t : warningTrend) {
                    fillRow(s7, r7++, dataStyle(wb),
                            String.valueOf(t.getOrDefault("date", "")),
                            String.valueOf(t.getOrDefault("cnt", 0)));
                }
            } else {
                fillRow(s7, r7, dataStyle(wb), "暂无数据", "");
            }
            fitColumns(s7, 2, 10, 3);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ===== 辅助 =====

    private static long toLong(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return 0; }
    }

    private static String findCnt(List<Map<String, Object>> list, String community) {
        if (list == null) return "0";
        for (Map<String, Object> m : list) {
            if (community.equals(m.get("community"))) return String.valueOf(m.getOrDefault("cnt", 0));
        }
        return "0";
    }

    private static String findTotal(List<Map<String, Object>> list, String community) {
        if (list == null) return "0";
        for (Map<String, Object> m : list) {
            if (community.equals(m.get("community"))) return String.valueOf(m.getOrDefault("total", 0));
        }
        return "0";
    }

    private static String findOnline(List<Map<String, Object>> list, String community) {
        if (list == null) return "0";
        for (Map<String, Object> m : list) {
            if (community.equals(m.get("community"))) return String.valueOf(m.getOrDefault("online", 0));
        }
        return "0";
    }

    private static String str(Map<String, Object> m, String key) {
        if (m == null) return "0";
        return String.valueOf(m.getOrDefault(key, 0));
    }
}
