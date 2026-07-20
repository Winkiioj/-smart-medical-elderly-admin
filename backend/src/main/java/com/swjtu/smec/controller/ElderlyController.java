package com.swjtu.smec.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Elderly;
import com.swjtu.smec.entity.FamilyContact;
import com.swjtu.smec.service.ElderlyService;
import com.swjtu.smec.service.FamilyContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 老人档案 Controller — UC-DOC-02
 */
@Tag(name = "老人档案", description = "老人基本信息的增删改查 + 家属管理")
@RestController
@RequestMapping("/api/elderly")
public class ElderlyController {

    @Autowired
    private ElderlyService elderlyService;

    @Autowired
    private FamilyContactService familyContactService;

    /**
     * 分页查询老人列表
     */
    @Operation(summary = "分页查询老人列表(多维度筛选)")
    @GetMapping("/list")
    public CommonResult<IPage<Elderly>> list(
            @RequestParam Long doctorId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String community,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) Integer ageMin,
            @RequestParam(required = false) Integer ageMax,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        LocalDate sd = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate ed = endDate != null ? LocalDate.parse(endDate) : null;
        IPage<Elderly> result = elderlyService.page(doctorId, keyword, community,
                gender, ageMin, ageMax, status, tagId, sd, ed, page, size);
        return CommonResult.success(result);
    }

    /**
     * 老人详情（含家属列表）
     */
    @Operation(summary = "查询老人详情")
    @GetMapping("/{id}")
    public CommonResult<Map<String, Object>> detail(@PathVariable Long id) {
        Elderly elderly = elderlyService.getById(id);
        if (elderly == null) {
            return CommonResult.error(404, "老人不存在");
        }
        List<FamilyContact> contacts = familyContactService.listByElderlyId(id);
        return CommonResult.success(Map.of("elderly", elderly, "contacts", contacts));
    }

    /**
     * 新增老人
     */
    @Operation(summary = "新增老人档案")
    @PostMapping
    public CommonResult<?> add(@RequestBody Map<String, Object> body) {
        Elderly elderly = parseElderly(body);
        // 身份证号解析性别和出生日期
        String idCard = elderly.getIdCard();
        if (idCard != null && idCard.length() == 18) {
            // 出生日期
            try {
                String birth = idCard.substring(6, 10) + "-" + idCard.substring(10, 12) + "-" + idCard.substring(12, 14);
                LocalDate bd = LocalDate.parse(birth);
                elderly.setBirthDate(bd);
                // 年龄
                if (elderly.getAge() == null) {
                    elderly.setAge(java.time.Period.between(bd, LocalDate.now()).getYears());
                }
            } catch (Exception ignored) {}
            // 性别：第17位奇数=男 偶数=女
            int genderCode = Integer.parseInt(idCard.substring(16, 17));
            if (elderly.getGender() == null) {
                elderly.setGender(genderCode % 2 == 1 ? 1 : 2);
            }
        }
        elderlyService.save(elderly);

        // 家属列表（可选）
        Object contactsObj = body.get("contacts");
        if (contactsObj instanceof List) {
            List<FamilyContact> contacts = new java.util.ArrayList<>();
            for (Object obj : (List<?>) contactsObj) {
                contacts.add(parseContact(obj, elderly.getId()));
            }
            familyContactService.saveBatch(contacts);
        }
        return CommonResult.success(elderly.getId());
    }

    /**
     * 编辑老人
     */
    @Operation(summary = "编辑老人档案")
    @PutMapping("/{id}")
    public CommonResult<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Elderly elderly = parseElderly(body);
        elderly.setId(id);
        elderlyService.updateById(elderly);

        // 家属：先删后插（在同一事务中）
        Object contactsObj = body.get("contacts");
        if (contactsObj instanceof List) {
            List<FamilyContact> contacts = new java.util.ArrayList<>();
            for (Object obj : (List<?>) contactsObj) {
                contacts.add(parseContact(obj, id));
            }
            familyContactService.replaceByElderlyId(id, contacts);
        }
        return CommonResult.success(null);
    }

    /**
     * 查某老人的家属列表
     */
    @Operation(summary = "查询家属列表")
    @GetMapping("/{elderlyId}/contacts")
    public CommonResult<List<FamilyContact>> contacts(@PathVariable Long elderlyId) {
        return CommonResult.success(familyContactService.listByElderlyId(elderlyId));
    }

    // ===== 工具方法 =====
    private Elderly parseElderly(Map<String, Object> body) {
        Elderly e = new Elderly();
        e.setName((String) body.get("name"));
        e.setIdCard((String) body.get("idCard"));
        e.setPhone((String) body.get("phone"));
        e.setAddress((String) body.get("address"));
        e.setCommunity((String) body.get("community"));
        e.setHeight(toDecimal(body.get("height")));
        e.setEmergencyContact((String) body.get("emergencyContact"));
        e.setEmergencyPhone((String) body.get("emergencyPhone"));
        e.setMedicalHistory((String) body.get("medicalHistory"));
        e.setRemark((String) body.get("remark"));
        if (body.get("gender") != null) e.setGender(toInt(body.get("gender")));
        if (body.get("doctorId") != null) e.setDoctorId(toLong(body.get("doctorId")));
        if (body.get("age") != null) e.setAge(toInt(body.get("age")));
        if (body.get("birthDate") != null) e.setBirthDate(parseDate((String) body.get("birthDate")));
        if (body.get("admissionDate") != null) e.setAdmissionDate(parseDate((String) body.get("admissionDate")));
        else e.setAdmissionDate(LocalDate.now());
        return e;
    }

    private FamilyContact parseContact(Object obj, Long elderlyId) {
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) obj;
        FamilyContact c = new FamilyContact();
        c.setElderlyId(elderlyId);
        c.setName((String) m.get("name"));
        c.setRelationship((String) m.get("relationship"));
        c.setPhone((String) m.get("phone"));
        c.setBackupPhone((String) m.get("backupPhone"));
        c.setAddress((String) m.get("address"));
        if (m.get("isEmergency") != null) c.setIsEmergency(((Number) m.get("isEmergency")).intValue());
        return c;
    }

    private Integer toInt(Object v) { if (v == null) return null; return ((Number) v).intValue(); }
    private Long toLong(Object v) { if (v == null) return null; return ((Number) v).longValue(); }
    private java.math.BigDecimal toDecimal(Object v) { if (v == null) return null; return new java.math.BigDecimal(v.toString()); }
    private LocalDate parseDate(String s) { try { return LocalDate.parse(s); } catch (Exception e) { return null; } }
}
