package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.entity.SysMenu;
import com.swjtu.smec.entity.SysRoleMenu;
import com.swjtu.smec.entity.SysUserRole;
import com.swjtu.smec.mapper.SysMenuMapper;
import com.swjtu.smec.mapper.SysRoleMenuMapper;
import com.swjtu.smec.mapper.SysUserRoleMapper;
import com.swjtu.smec.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu>
        implements SysMenuService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<SysMenu> getMenusByUserId(Long userId) {
        // 1. 查用户的角色
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );
        if (userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 查角色对应的菜单
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds)
        );
        if (roleMenus.isEmpty()) {
            return new ArrayList<>();
        }

        // 3. 查菜单详情
        List<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).distinct().toList();
        return this.baseMapper.selectBatchIds(menuIds);
    }
}
