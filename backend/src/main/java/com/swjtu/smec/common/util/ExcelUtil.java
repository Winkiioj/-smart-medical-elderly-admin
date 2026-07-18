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

    // ==================== 全局总览 Sheet ====================

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

            for (int i = 0; i < headers.length; i++) s1.autoSizeColumn(i);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ==================== 社区详情 Sheet ====================

    @SuppressWarnings("unchecked")
    public static byte[] exportCommunityDetail(Map<String, Object> data) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            String community = (String) data.get("community");
            // Sheet 1: 概览
            Sheet s1 = wb.createSheet("社区概览");
            s1.addMergedRegion(new CellRangeAddress(0, 0, 0, 2));
            fillRow(s1, 0, titleStyle(wb), (community != null ? community : "") + " — 社区详情报表");

            int r = 2;
            Map<String, Object> elderly = (Map<String, Object>) data.get("elderly");
            Map<String, Object> devices = (Map<String, Object>) data.get("devices");
            Map<String, Object> warnings = (Map<String, Object>) data.get("warnings");

            fillRow(s1, r++, headerStyle(wb), "指标", "数值", "备注");
            fillRow(s1, r++, dataStyle(wb), "在院老人", str(elderly, "total"), "含已签约" + str(elderly, "assigned") + "人");
            fillRow(s1, r++, dataStyle(wb), "签约医生", String.valueOf(((List<?>) data.getOrDefault("doctorWorkload", List.of())).size()), "");
            fillRow(s1, r++, dataStyle(wb), "在线设备", str(devices, "online"), "共" + str(devices, "total") + "台");
            fillRow(s1, r++, dataStyle(wb), "待处理预警", str(warnings, "pending"), "共" + str(warnings, "total") + "条");
            for (int i = 0; i < 3; i++) s1.autoSizeColumn(i);

            // Sheet 2: 医生工作量
            Sheet s2 = wb.createSheet("医生工作量");
            fillRow(s2, 0, headerStyle(wb), "医生姓名", "签约老人数");
            List<Map<String, Object>> workload = (List<Map<String, Object>>) data.get("doctorWorkload");
            int r2 = 1;
            if (workload != null) {
                for (Map<String, Object> d : workload) {
                    fillRow(s2, r2++, dataStyle(wb),
                            String.valueOf(d.getOrDefault("real_name", "")),
                            String.valueOf(d.getOrDefault("signing_count", 0)));
                }
            }
            s2.autoSizeColumn(0); s2.autoSizeColumn(1);

            // Sheet 3: 近30天新增趋势
            Sheet s3 = wb.createSheet("30天新增趋势");
            fillRow(s3, 0, headerStyle(wb), "日期", "新增人数");
            List<Map<String, Object>> trend = (List<Map<String, Object>>) data.get("admissionTrend");
            int r3 = 1;
            if (trend != null) {
                for (Map<String, Object> t : trend) {
                    fillRow(s3, r3++, dataStyle(wb),
                            String.valueOf(t.getOrDefault("date", "")),
                            String.valueOf(t.getOrDefault("cnt", 0)));
                }
            }
            s3.autoSizeColumn(0); s3.autoSizeColumn(1);

            wb.write(out);
            return out.toByteArray();
        }
    }

    // ===== 辅助 =====
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
