package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.ElderlyTagMapping;
import com.swjtu.smec.mapper.ElderlyTagMappingMapper;
import com.swjtu.smec.service.ElderlyTagMappingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 老人标签关联 Service 实现
 *
 * @author C
 */
@Service
@Transactional
public class ElderlyTagMappingServiceImpl
        extends ServiceImpl<ElderlyTagMappingMapper, ElderlyTagMapping>
        implements ElderlyTagMappingService {

    @Override
    public CommonResult getTagIdsByElderly(Long elderlyId) {
        List<ElderlyTagMapping> mappings = this.baseMapper.selectByElderlyId(elderlyId);
        List<Long> tagIds = mappings.stream()
                .map(ElderlyTagMapping::getTagId)
                .collect(Collectors.toList());
        return CommonResult.success(tagIds);
    }

    @Override
    public CommonResult saveTags(Long elderlyId, List<Long> tagIds) {
        // 先删后插
        this.baseMapper.deleteByElderlyId(elderlyId);
        if (tagIds != null && !tagIds.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (Long tagId : tagIds) {
                ElderlyTagMapping mapping = new ElderlyTagMapping();
                mapping.setElderlyId(elderlyId);
                mapping.setTagId(tagId);
                mapping.setCreateTime(now);
                this.baseMapper.insert(mapping);
            }
        }
        return CommonResult.success(null);
    }
}
