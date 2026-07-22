package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.entity.SysUser;

import java.util.Map;

public interface SysUserService extends IService<SysUser> {

    // ===== 认证相关 =====
    /**
     * 登录，返回 token + 角色信息
     */
    Map<String, Object> login(String username, String password);
    void logout(String token);

    // ===== 医生管理 =====

    /**
     * 分页查询某社区下所有医生（含签约数）
     */
    Page<Map<String, Object>> pageDoctors(String community, int pageNo, int pageSize, String keyword);

    /** 医生档案（多角色只读）：支持 gender/status 筛选，community 可为 null（ORG 查全部） */
    Page<Map<String, Object>> pageDoctorsArchive(String community, int pageNo, int pageSize,
                                                  String keyword, Integer gender, Integer status);

    /**
     * 新增医生（自动创建账号，角色=DOCTOR）
     * @return 初始密码
     */
    String createDoctor(SysUser doctor, String community);

    /**
     * 编辑医生信息（姓名/邮箱/性别/备注）
     */
    void updateDoctor(SysUser doctor);

    /**
     * 启用/禁用医生账号
     */
    void toggleDoctorStatus(Long doctorId, Integer status);

    // ===== 供 B/C 跨域调用 =====
    SysUser getById(Long id);
    java.util.List<SysUser> listDoctorsByCommunity(String community);
    /**
     * 统计某医生的签约老人数 [供 A 自己在医生列表中使用]
     */
    Long countSigningElderly(Long doctorId);
}
