package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据用户ID获取其角色对应的菜单列表
     */
    List<SysMenu> getMenusByUserId(Long userId);
}
