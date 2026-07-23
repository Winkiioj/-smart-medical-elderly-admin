package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.entity.Elderly;
import com.swjtu.smec.mapper.ElderlyMapper;
import com.swjtu.smec.service.ElderlyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ElderlyServiceImpl extends ServiceImpl<ElderlyMapper, Elderly>
        implements ElderlyService {

    @Override
    public IPage<Elderly> page(Long doctorId, String keyword, String community,
                                Integer gender, Integer ageMin, Integer ageMax,
                                Integer status, Long tagId,
                                LocalDate startDate, LocalDate endDate,
                                int page, int size) {
        LambdaQueryWrapper<Elderly> wrapper = new LambdaQueryWrapper<>();

        // 数据权限：签约医生（仅医生传入，ORG/COM不传则查全部）
        if (doctorId != null) {
            wrapper.eq(Elderly::getDoctorId, doctorId);
        }

        // 关键词
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Elderly::getName, keyword)
                             .or().like(Elderly::getIdCard, keyword));
        }
        // 社区
        if (StringUtils.hasText(community)) {
            wrapper.eq(Elderly::getCommunity, community);
        }
        // 性别
        if (gender != null) {
            wrapper.eq(Elderly::getGender, gender);
        }
        // 年龄段
        if (ageMin != null && ageMax != null) {
            wrapper.between(Elderly::getAge, ageMin, ageMax);
        }
        // 状态（不传默认查在院；-1 表示不过滤）
        if (status != null && status != -1) {
            wrapper.eq(Elderly::getStatus, status);
        } else if (status == null) {
            wrapper.eq(Elderly::getStatus, 1);
        }
        // 标签筛选：通过子查询 elderly_tag_mapping 表
        if (tagId != null) {
            wrapper.inSql(Elderly::getId,
                "SELECT elderly_id FROM elderly_tag_mapping WHERE tag_id = " + tagId);
        }
        // 入档日期范围
        if (startDate != null) {
            wrapper.ge(Elderly::getAdmissionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Elderly::getAdmissionDate, endDate);
        }

        wrapper.orderByDesc(Elderly::getAdmissionDate);
        return this.baseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public Elderly getById(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public List<Elderly> listByDoctorId(Long doctorId) {
        LambdaQueryWrapper<Elderly> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Elderly::getDoctorId, doctorId)
               .eq(Elderly::getStatus, 1)
               .orderByAsc(Elderly::getName);
        return this.baseMapper.selectList(wrapper);
    }
}
