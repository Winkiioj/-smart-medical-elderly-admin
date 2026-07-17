package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.ElderlyTag;

/**
 * 老人标签 Service
 *
 * @author C
 */
public interface ElderlyTagService extends IService<ElderlyTag> {

    /** 查询所有标签 */
    CommonResult listAll();

    /** 新增标签 */
    CommonResult add(ElderlyTag tag);

    /** 编辑标签 */
    CommonResult update(ElderlyTag tag);

    /** 删除标签 */
    CommonResult delete(Long id);
}
