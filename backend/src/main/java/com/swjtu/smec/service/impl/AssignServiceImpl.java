package com.swjtu.smec.service.impl;

import com.swjtu.smec.service.AssignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class AssignServiceImpl implements AssignService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int DOCTOR_MAX_LOAD = 300;

    @Override
    public Map<String, Object> listUnassigned(String community, int pageNo, int pageSize, String keyword) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, name, gender, age, phone, address, admission_date, " +
            "  CONCAT(LEFT(id_card, 4), '**********', RIGHT(id_card, 4)) AS id_card_masked " +
            "FROM elderly WHERE community = ? AND doctor_id IS NULL AND is_deleted = 0 AND status = 1 ");
        List<Object> params = new ArrayList<>();
        params.add(community);
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (name LIKE ? OR phone LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        sql.append("ORDER BY admission_date DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((pageNo - 1) * pageSize);

        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        String countSql = "SELECT COUNT(*) FROM elderly WHERE community = ? AND doctor_id IS NULL AND is_deleted = 0 AND status = 1";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class, community);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total != null ? total : 0);
        return result;
    }

    @Override
    public Map<String, Object> listAssigned(String community, int pageNo, int pageSize,
                                             Long doctorId, String keyword) {
        StringBuilder sql = new StringBuilder(
            "SELECT e.id, e.name, e.gender, e.age, e.phone, e.address, e.admission_date, " +
            "  e.doctor_id, u.real_name AS doctor_name, " +
            "  CONCAT(LEFT(e.id_card, 4), '**********', RIGHT(e.id_card, 4)) AS id_card_masked " +
            "FROM elderly e LEFT JOIN sys_user u ON e.doctor_id = u.id " +
            "WHERE e.community = ? AND e.doctor_id IS NOT NULL AND e.is_deleted = 0 AND e.status = 1 ");
        List<Object> params = new ArrayList<>();
        params.add(community);

        if (doctorId != null) {
            sql.append("AND e.doctor_id = ? ");
            params.add(doctorId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (e.name LIKE ? OR e.phone LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        sql.append("ORDER BY e.admission_date DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((pageNo - 1) * pageSize);

        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        // count
        StringBuilder countSql = new StringBuilder(
            "SELECT COUNT(*) FROM elderly e WHERE e.community = ? AND e.doctor_id IS NOT NULL AND e.is_deleted = 0 AND e.status = 1 ");
        List<Object> countParams = new ArrayList<>();
        countParams.add(community);
        if (doctorId != null) {
            countSql.append("AND e.doctor_id = ? ");
            countParams.add(doctorId);
        }
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, countParams.toArray());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total != null ? total : 0);
        return result;
    }

    @Override
    public List<Map<String, Object>> getDoctorLoad(String community) {
        String sql = "SELECT u.id, u.real_name, u.phone, " +
            "  COALESCE(COUNT(e.id), 0) AS signing_count " +
            "FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.role_id = 3 " +
            "LEFT JOIN elderly e ON e.doctor_id = u.id AND e.is_deleted = 0 AND e.status = 1 " +
            "WHERE u.community = ? AND u.status = 1 AND u.is_deleted = 0 " +
            "GROUP BY u.id, u.real_name, u.phone " +
            "ORDER BY signing_count DESC";
        return jdbcTemplate.queryForList(sql, community);
    }

    @Override
    public void assignDoctor(Long elderlyId, Long doctorId) {
        checkDoctorLoad(doctorId);
        jdbcTemplate.update("UPDATE elderly SET doctor_id = ? WHERE id = ? AND doctor_id IS NULL", doctorId, elderlyId);
    }

    @Override
    public int batchAssign(List<Long> elderlyIds, Long doctorId) {
        int current = getCurrentLoad(doctorId);
        int capacity = DOCTOR_MAX_LOAD - current;
        if (capacity <= 0) {
            throw new RuntimeException("该医生签约数已达上限（" + DOCTOR_MAX_LOAD + "人），无法继续分配");
        }
        int assignCount = Math.min(elderlyIds.size(), capacity);
        for (int i = 0; i < assignCount; i++) {
            jdbcTemplate.update("UPDATE elderly SET doctor_id = ? WHERE id = ? AND doctor_id IS NULL", doctorId, elderlyIds.get(i));
        }
        return assignCount;
    }

    @Override
    public void reassignDoctor(Long elderlyId, Long newDoctorId) {
        checkDoctorLoad(newDoctorId);
        jdbcTemplate.update("UPDATE elderly SET doctor_id = ? WHERE id = ?", newDoctorId, elderlyId);
    }

    private void checkDoctorLoad(Long doctorId) {
        int current = getCurrentLoad(doctorId);
        if (current >= DOCTOR_MAX_LOAD) {
            throw new RuntimeException("该医生签约数已达上限（" + DOCTOR_MAX_LOAD + "人），无法继续分配");
        }
    }

    private int getCurrentLoad(Long doctorId) {
        String sql = "SELECT COUNT(*) FROM elderly WHERE doctor_id = ? AND is_deleted = 0 AND status = 1";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, doctorId);
        return count != null ? count.intValue() : 0;
    }
}
