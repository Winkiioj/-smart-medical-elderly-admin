package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.entity.WarningRule;

import java.util.List;

/**
 * 预警规则 Service 接口 — C 负责
 *
 * @author C
 */
public interface WarningRuleService extends IService<WarningRule> {

    /**
     * [供 B 跨域调用] 查询所有已启用的预警规则
     */
    List<WarningRule> listEnabled();
}
