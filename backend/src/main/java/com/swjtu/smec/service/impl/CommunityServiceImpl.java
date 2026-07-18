package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Community;
import com.swjtu.smec.mapper.CommunityMapper;
import com.swjtu.smec.service.CommunityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 社区管理 Service 实现 — A 负责
 *
 * @author A
 */
@Service
@Transactional
public class CommunityServiceImpl
        extends ServiceImpl<CommunityMapper, Community>
        implements CommunityService {

    @Override
    public CommonResult page(int pageNo, int pageSize, String keyword) {
        LambdaQueryWrapper<Community> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Community::getName, keyword);
        }
        wrapper.orderByAsc(Community::getId);
        Page<Community> page = this.baseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return CommonResult.success(page.getRecords(), page.getTotal());
    }

    @Override
    public CommonResult add(Community community) {
        Community exist = this.baseMapper.selectByName(community.getName());
        if (exist != null) {
            return CommonResult.error(400, "社区名称已存在：" + community.getName());
        }
        community.setStatus(1);
        this.baseMapper.insert(community);
        return CommonResult.success(community);
    }

    @Override
    public CommonResult update(Community community) {
        Community db = this.baseMapper.selectById(community.getId());
        if (db == null) return CommonResult.error(404, "社区不存在");

        // 名称变更需验重
        if (!db.getName().equals(community.getName())) {
            Community dup = this.baseMapper.selectByName(community.getName());
            if (dup != null) return CommonResult.error(400, "社区名称已存在：" + community.getName());
        }
        // 只改 name/address/contactName/contactPhone/remark
        db.setName(community.getName());
        db.setAddress(community.getAddress());
        db.setContactName(community.getContactName());
        db.setContactPhone(community.getContactPhone());
        db.setRemark(community.getRemark());
        this.baseMapper.updateById(db);
        return CommonResult.success(null);
    }

    @Override
    public CommonResult toggleStatus(Long id, Integer status) {
        Community db = this.baseMapper.selectById(id);
        if (db == null) return CommonResult.error(404, "社区不存在");
        db.setStatus(status);
        this.baseMapper.updateById(db);
        return CommonResult.success(null);
    }

    @Override
    public CommonResult listEnabled() {
        return CommonResult.success(this.baseMapper.listEnabled());
    }

    // ===== 供 B/C 跨域 =====

    @Override
    public Community getByName(String name) {
        return this.baseMapper.selectByName(name);
    }

    @Override
    public List<Community> listAllEnabled() {
        return this.baseMapper.listEnabled();
    }
}
