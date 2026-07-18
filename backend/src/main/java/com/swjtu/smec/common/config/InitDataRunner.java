package com.swjtu.smec.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swjtu.smec.entity.*;
import com.swjtu.smec.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用首次启动时的数据初始化
 * <p>
 * - 检测 sys_role 表是否为空
 * - 为空则插入 4 个角色 + 菜单树 + 5 个测试账号
 * - 将明文密码自动升级为 BCrypt 加密
 */
@Component
public class InitDataRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InitDataRunner.class);

    @Autowired private SysRoleMapper sysRoleMapper;
    @Autowired private SysMenuMapper sysMenuMapper;
    @Autowired private SysRoleMenuMapper sysRoleMenuMapper;
    @Autowired private SysUserMapper sysUserMapper;
    @Autowired private SysUserRoleMapper sysUserRoleMapper;
    @Autowired private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 检查是否已初始化
        if (sysRoleMapper.selectCount(null) > 0) {
            log.info("[InitData] 数据已初始化，跳过");
            upgradePasswords();  // 每次启动都检查是否需要加密明文密码
            return;
        }

        log.info("[InitData] 首次启动，开始初始化种子数据...");

        // 1. 插入角色
        SysRole role1 = insertRole("ORG_ADMIN", "机构管理员", "卫健委/平台最高管理者");
        SysRole role2 = insertRole("COM_ADMIN", "社区管理员", "管理本社区医生和老人");
        SysRole role3 = insertRole("DOCTOR", "社区医生", "管理签约老人、处理预警随访");
        SysRole role4 = insertRole("DEVICE_ADMIN", "设备管理员", "管理设备台账和状态");

        // 2. 插入菜单
        SysMenu menu1  = insertMenu(0L, "数据看板", "/dashboard", "HomeFilled", 1, 1);
        SysMenu menu2  = insertMenu(0L, "报表统计", "/reports", "Document", 1, 2);
        SysMenu menu10 = insertMenu(0L, "医生管理", "/doctors", "UserFilled", 1, 3);
        SysMenu menu11 = insertMenu(0L, "老人分配", "/assign", "Connection", 1, 4);
        SysMenu menu12 = insertMenu(0L, "社区报表", "/com-reports", "DataLine", 1, 5);
        SysMenu menu20 = insertMenu(0L, "工作台", "/doctor-dashboard", "HomeFilled", 1, 6);
        SysMenu menu21 = insertMenu(0L, "老人档案", "/elderly", "User", 1, 7);
        SysMenu menu22 = insertMenu(0L, "健康管理", "/health", "Monitor", 0, 8);
        SysMenu menu23 = insertMenu(menu22.getId(), "数据导入", "/health-import", "Upload", 1, 1);
        SysMenu menu24 = insertMenu(menu22.getId(), "趋势图", "/health-trend", "TrendCharts", 1, 2);
        SysMenu menu25 = insertMenu(0L, "预警管理", "/warnings", "Warning", 1, 9);
        SysMenu menu26 = insertMenu(0L, "随访管理", "/followup", "Calendar", 1, 10);
        SysMenu menu27 = insertMenu(0L, "评估报告", "/assessment", "Document", 1, 11);
        SysMenu menu30 = insertMenu(0L, "设备台账", "/devices", "Cpu", 1, 12);
        SysMenu menu31 = insertMenu(0L, "设备监控", "/device-monitor", "VideoCamera", 1, 13);
        SysMenu menu32 = insertMenu(0L, "维修管理", "/device-repair", "SetUp", 1, 14);

        // 3. 分配菜单给角色
        SysMenu[] allMenus = {menu1, menu2, menu10, menu11, menu12,
                menu20, menu21, menu22, menu23, menu24, menu25, menu26, menu27,
                menu30, menu31, menu32};
        for (SysMenu m : allMenus) {
            insertRoleMenu(role1.getId(), m.getId());  // 机构管理员 → 全部
        }
        insertRoleMenu(role2.getId(), menu10.getId());  // 社区管理员
        insertRoleMenu(role2.getId(), menu11.getId());
        insertRoleMenu(role2.getId(), menu12.getId());
        for (SysMenu m : new SysMenu[]{menu20, menu21, menu22, menu23, menu24, menu25, menu26, menu27}) {
            insertRoleMenu(role3.getId(), m.getId());  // 社区医生
        }
        for (SysMenu m : new SysMenu[]{menu30, menu31, menu32}) {
            insertRoleMenu(role4.getId(), m.getId());  // 设备管理员
        }

        // 4. 插入测试账号（明文密码，下面 upgradePasswords 会自动加密）
        insertUser("admin", "123456", "王凯", "13800000000", null, role1);
        insertUser("community_adm", "123456", "张社区", "13800000001", "花园社区", role2);
        insertUser("doctor_zhang", "123456", "张医生", "13800000002", "花园社区", role3);
        insertUser("doctor_li", "123456", "李医生", "13800000003", "花园社区", role3);
        insertUser("device_adm", "123456", "刘设备", "13800000004", null, role4);

        // 5. 加密密码
        upgradePasswords();

        log.info("[InitData] 种子数据初始化完成！5个角色、16个菜单、5个测试账号");
    }

    /**
     * 将数据库中所有非 BCrypt 密码升级为 BCrypt 加密
     */
    private void upgradePasswords() {
        List<SysUser> users = sysUserMapper.selectList(null);
        int count = 0;
        for (SysUser user : users) {
            String pwd = user.getPassword();
            // 升级 {noop} 标记的明文密码 或 纯明文密码
            if (pwd != null && !pwd.startsWith("$2a$") && !pwd.startsWith("$2b$")) {
                String plain = pwd.startsWith("{noop}") ? pwd.substring(6) : pwd;
                user.setPassword(passwordEncoder.encode(plain));
                sysUserMapper.updateById(user);
                count++;
            }
        }
        if (count > 0) {
            log.info("[InitData] 已加密 {} 个账号的密码", count);
        }
    }

    // --- 辅助插入方法 ---

    private SysRole insertRole(String code, String name, String desc) {
        SysRole r = new SysRole();
        r.setRoleCode(code);
        r.setRoleName(name);
        r.setDescription(desc);
        r.setStatus(1);
        sysRoleMapper.insert(r);
        return r;
    }

    private SysMenu insertMenu(Long parentId, String name, String path, String icon, int type, int sort) {
        SysMenu m = new SysMenu();
        m.setParentId(parentId);
        m.setMenuName(name);
        m.setPath(path);
        m.setIcon(icon);
        m.setMenuType(type);
        m.setSortOrder(sort);
        m.setStatus(1);
        sysMenuMapper.insert(m);
        return m;
    }

    private void insertRoleMenu(Long roleId, Long menuId) {
        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(roleId);
        rm.setMenuId(menuId);
        sysRoleMenuMapper.insert(rm);
    }

    private void insertUser(String username, String pwd, String realName,
                            String phone, String community, SysRole role) {
        SysUser u = new SysUser();
        u.setUsername(username);
        u.setPassword(pwd);
        u.setRealName(realName);
        u.setPhone(phone);
        u.setCommunity(community);
        u.setStatus(1);
        sysUserMapper.insert(u);

        SysUserRole ur = new SysUserRole();
        ur.setUserId(u.getId());
        ur.setRoleId(role.getId());
        sysUserRoleMapper.insert(ur);
    }
}
