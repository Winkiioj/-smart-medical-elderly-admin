package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.ElderlyTag;
import com.swjtu.smec.mapper.ElderlyTagMapper;
import com.swjtu.smec.service.ElderlyTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 老人标签 Service 实现
 *
 * @author C
 */
@Service
@Transactional
public class ElderlyTagServiceImpl
        extends ServiceImpl<ElderlyTagMapper, ElderlyTag>
        implements ElderlyTagService {

    @Override
    public CommonResult listAll() {
        return CommonResult.success(this.baseMapper.selectList(null));
    }

    @Override
    public CommonResult add(ElderlyTag tag) {
        if (tag.getColor() == null || tag.getColor().isEmpty()) {
            tag.setColor("#409EFF");
        }
        tag.setCreateTime(LocalDateTime.now());
        this.baseMapper.insert(tag);
        return CommonResult.success(tag);
    }

    @Override
    public CommonResult update(ElderlyTag tag) {
        ElderlyTag db = this.baseMapper.selectById(tag.getId());
        if (db == null) {
            return CommonResult.error(404, "标签不存在");
        }
        // 不修改创建时间
        tag.setCreateTime(null);
        this.baseMapper.updateById(tag);
        return CommonResult.success(null);
    }

    @Override
    public CommonResult delete(Long id) {
        ElderlyTag db = this.baseMapper.selectById(id);
        if (db == null) {
            return CommonResult.error(404, "标签不存在");
        }
        this.baseMapper.deleteById(id);
        return CommonResult.success(null);
    }
}
