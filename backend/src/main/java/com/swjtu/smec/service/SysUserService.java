package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.entity.SysUser;

/**
 * 用户 Service — 对外暴露的只读方法供 B/C 跨域调用
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 登录 — 验证用户名密码，返回 Token
     */
    String login(String username, String password);

    /**
     * 退出登录 — 清除 Redis 中的 Token
     */
    void logout(String token);

    /**
     * [供 B/C 跨域调用] 根据ID查用户
     */
    SysUser getById(Long id);

    /**
     * [供 B 调用] 查询某社区下所有启用状态的医生列表
     */
    java.util.List<SysUser> listDoctorsByCommunity(String community);
}
