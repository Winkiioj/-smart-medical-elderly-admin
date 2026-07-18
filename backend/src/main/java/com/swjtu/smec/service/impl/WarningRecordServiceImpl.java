package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.entity.WarningRecord;
import com.swjtu.smec.mapper.WarningRecordMapper;
import com.swjtu.smec.service.WarningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 预警记录 Service 实现 — C 负责
 *
 * @author C
 */
@Service
@Transactional
public class WarningRecordServiceImpl
        extends ServiceImpl<WarningRecordMapper, WarningRecord>
        implements WarningRecordService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void create(WarningRecord record) {
        record.setCreateTime(LocalDateTime.now());
        this.baseMapper.insert(record);
    }

    @Override
    public boolean existsWithin24h(Long elderlyId, String alertType) {
        LambdaQueryWrapper<WarningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRecord::getElderlyId, elderlyId)
               .eq(WarningRecord::getAlertType, alertType)
               .ge(WarningRecord::getCreateTime, LocalDateTime.now().minusHours(24));
        return this.baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public int countPendingByDoctorId(Long doctorId) {
        // 联表：warning_record.elderly_id → elderly.id → elderly.doctor_id
        String sql = "SELECT COUNT(*) FROM warning_record wr " +
                "JOIN elderly e ON wr.elderly_id = e.id " +
                "WHERE e.doctor_id = ? AND wr.status IN (0, 1) AND e.is_deleted = 0";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, doctorId);
        return count != null ? count : 0;
    }

    @Override
    public int closePendingByDoctorId(Long doctorId) {
        // 关闭该医生的所有待处理预警
        String sql = "UPDATE warning_record SET status = 3 WHERE status = 0 " +
                "AND (handler_id = ? OR elderly_id IN " +
                "(SELECT id FROM elderly WHERE doctor_id = ? AND is_deleted = 0))";
        return jdbcTemplate.update(sql, doctorId, doctorId);
    }
}
