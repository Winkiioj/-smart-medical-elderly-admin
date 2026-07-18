package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.entity.WarningRule;
import com.swjtu.smec.mapper.WarningRuleMapper;
import com.swjtu.smec.service.WarningRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 预警规则 Service 实现 — C 负责
 *
 * @author C
 */
@Service
@Transactional
public class WarningRuleServiceImpl
        extends ServiceImpl<WarningRuleMapper, WarningRule>
        implements WarningRuleService {

    @Override
    public List<WarningRule> listEnabled() {
        LambdaQueryWrapper<WarningRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarningRule::getIsEnabled, 1);
        return this.baseMapper.selectList(wrapper);
    }
}
