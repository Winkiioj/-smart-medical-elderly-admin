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
                                Integer ageMin, Integer ageMax,
                                LocalDate startDate, LocalDate endDate,
                                int page, int size) {
        LambdaQueryWrapper<Elderly> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(Elderly::getDoctorId, doctorId);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Elderly::getName, keyword)
                             .or().like(Elderly::getIdCard, keyword));
        }
        if (StringUtils.hasText(community)) {
            wrapper.eq(Elderly::getCommunity, community);
        }
        if (ageMin != null && ageMax != null) {
            wrapper.between(Elderly::getAge, ageMin, ageMax);
        }
        // 日期范围筛选（用于"本月新增"）
        if (startDate != null) {
            wrapper.ge(Elderly::getAdmissionDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Elderly::getAdmissionDate, endDate);
        }

        wrapper.eq(Elderly::getStatus, 1);
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
