package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.ElderlyTagMapping;

import java.util.List;

/**
 * 老人标签关联 Service
 *
 * @author C
 */
public interface ElderlyTagMappingService extends IService<ElderlyTagMapping> {

    /** 查询某老人的所有标签ID */
    CommonResult getTagIdsByElderly(Long elderlyId);

    /** 保存某老人的标签（先删后插） */
    CommonResult saveTags(Long elderlyId, List<Long> tagIds);
}
