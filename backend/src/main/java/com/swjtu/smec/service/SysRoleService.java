package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.entity.SysRole;

public interface SysRoleService extends IService<SysRole> {

    /**
     * [供 B/C 调用] 根据角色编码查角色
     */
    SysRole getByCode(String roleCode);
}
