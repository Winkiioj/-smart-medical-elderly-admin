package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.entity.SysUser;
import com.swjtu.smec.entity.SysUserRole;
import com.swjtu.smec.mapper.SysUserMapper;
import com.swjtu.smec.mapper.SysUserRoleMapper;
import com.swjtu.smec.service.SysUserService;
import com.swjtu.smec.service.WarningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private WarningRecordService warningRecordService;

    private static final Duration TOKEN_TTL = Duration.ofHours(8);
    private static final Long DOCTOR_ROLE_ID = 3L;

    // ========== 认证 ==========

    @Override
    public Map<String, Object> login(String username, String password) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = this.baseMapper.selectOne(wrapper);

        if (user == null) throw new RuntimeException("用户名或密码错误");
        if (user.getStatus() == 0) throw new RuntimeException("账号已被禁用，请联系管理员");

        boolean matches;
        String dbPwd = user.getPassword();
        if (dbPwd.startsWith("$2a$") || dbPwd.startsWith("$2b$")) {
            matches = passwordEncoder.matches(password, dbPwd);
        } else if (dbPwd.startsWith("{noop}")) {
            // Spring Security 明文标记：{noop}123456 → 剥离前缀后比较
            matches = password.equals(dbPwd.substring(6));
        } else {
            matches = password.equals(dbPwd);
        }
        if (!matches) throw new RuntimeException("用户名或密码错误");

        // 查询用户所有角色
        String roleSql = "SELECT r.id AS role_id, r.role_code, r.role_name FROM sys_role r " +
                "INNER JOIN sys_user_role ur ON r.id = ur.role_id WHERE ur.user_id = ?";
        List<Map<String, Object>> roles = jdbcTemplate.queryForList(roleSql, user.getId());

        // 用 FastJSON 序列化角色列表写入 Redis（兼容旧格式）
        String rolesJson = "[]";
        if (!roles.isEmpty()) {
            rolesJson = com.alibaba.fastjson.JSON.toJSONString(roles);
        }
        String firstRoleCode = roles.isEmpty() ? "" : (String) roles.get(0).get("role_code");
        String firstRoleName = roles.isEmpty() ? "" : (String) roles.get(0).get("role_name");

        String token = UUID.randomUUID().toString().replace("-", "");
        String userJson = "{\"userId\":" + user.getId()
                + ",\"username\":\"" + user.getUsername()
                + "\",\"realName\":\"" + (user.getRealName() != null ? user.getRealName() : "")
                + "\",\"community\":\"" + (user.getCommunity() != null ? user.getCommunity() : "")
                + "\",\"roleCode\":\"" + firstRoleCode
                + "\",\"roleName\":\"" + firstRoleName
                + "\",\"roles\":" + rolesJson
                + "}";
        redisTemplate.opsForValue().set("Token::" + token, userJson, TOKEN_TTL);

        user.setLastLoginTime(LocalDateTime.now());
        this.baseMapper.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("roleCode", firstRoleCode);
        result.put("roleName", firstRoleName);
        result.put("roles", roles);
        result.put("community", user.getCommunity() != null ? user.getCommunity() : "");
        result.put("realName", user.getRealName() != null ? user.getRealName() : "");
        return result;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete("Token::" + token);
    }

    // ========== 医生管理 ==========

    @Override
    public Page<Map<String, Object>> pageDoctorsArchive(String community, int pageNo, int pageSize,
                                                         String keyword, Integer gender, Integer status) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.id, u.real_name, u.phone, u.gender, u.email, u.status, u.community, ");
        sql.append("  u.create_time, ");
        sql.append("  (SELECT COUNT(*) FROM elderly e WHERE e.doctor_id = u.id AND e.is_deleted = 0) AS signing_count ");
        sql.append("FROM sys_user u ");
        sql.append("INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.role_id = ").append(DOCTOR_ROLE_ID).append(" ");
        sql.append("WHERE u.is_deleted = 0 ");

        List<Object> params = new ArrayList<>();

        if (community != null && !community.isEmpty()) {
            sql.append("AND u.community = ? ");
            params.add(community);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (u.real_name LIKE ? OR u.phone LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (gender != null) {
            sql.append("AND u.gender = ? ");
            params.add(gender);
        }
        if (status != null) {
            sql.append("AND u.status = ? ");
            params.add(status);
        }

        sql.append("ORDER BY u.create_time DESC ");
        sql.append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((pageNo - 1) * pageSize);

        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        // 查总数（同样条件，去掉 LIMIT/OFFSET）
        StringBuilder countParams = new StringBuilder();
        countParams.append("SELECT COUNT(*) FROM sys_user u ");
        countParams.append("INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.role_id = ").append(DOCTOR_ROLE_ID).append(" ");
        countParams.append("WHERE u.is_deleted = 0 ");
        List<Object> cp = new ArrayList<>();
        if (community != null && !community.isEmpty()) { countParams.append("AND u.community = ? "); cp.add(community); }
        if (keyword != null && !keyword.isEmpty()) { countParams.append("AND (u.real_name LIKE ? OR u.phone LIKE ?) "); cp.add("%" + keyword + "%"); cp.add("%" + keyword + "%"); }
        if (gender != null) { countParams.append("AND u.gender = ? "); cp.add(gender); }
        if (status != null) { countParams.append("AND u.status = ? "); cp.add(status); }
        Long total = jdbcTemplate.queryForObject(countParams.toString(), Long.class, cp.toArray());

        Page<Map<String, Object>> page = new Page<>(pageNo, pageSize);
        page.setRecords(records);
        page.setTotal(total != null ? total : 0);
        return page;
    }

    @Override
    public Page<Map<String, Object>> pageDoctors(String community, int pageNo, int pageSize, String keyword) {
        // 用 JdbcTemplate 直接写联表 SQL，因为要同时查签约数和角色过滤
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.id, u.real_name, u.phone, u.gender, u.email, u.status, ");
        sql.append("  u.create_time, ");
        sql.append("  (SELECT COUNT(*) FROM elderly e WHERE e.doctor_id = u.id AND e.is_deleted = 0) AS signing_count ");
        sql.append("FROM sys_user u ");
        sql.append("INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.role_id = ").append(DOCTOR_ROLE_ID).append(" ");
        sql.append("WHERE u.community = ? AND u.is_deleted = 0 ");

        List<Object> params = new ArrayList<>();
        params.add(community);

        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND (u.real_name LIKE ? OR u.phone LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        sql.append("ORDER BY u.create_time DESC ");
        sql.append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((pageNo - 1) * pageSize);

        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql.toString(), params.toArray());

        // 查总数
        StringBuilder countSql = new StringBuilder();
        countSql.append("SELECT COUNT(*) FROM sys_user u ");
        countSql.append("INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.role_id = ").append(DOCTOR_ROLE_ID).append(" ");
        countSql.append("WHERE u.community = ? AND u.is_deleted = 0 ");
        List<Object> countParams = new ArrayList<>();
        countParams.add(community);
        if (keyword != null && !keyword.isEmpty()) {
            countSql.append("AND (u.real_name LIKE ? OR u.phone LIKE ?) ");
            countParams.add("%" + keyword + "%");
            countParams.add("%" + keyword + "%");
        }
        Long total = jdbcTemplate.queryForObject(countSql.toString(), Long.class, countParams.toArray());

        Page<Map<String, Object>> page = new Page<>(pageNo, pageSize);
        page.setRecords(records);
        page.setTotal(total != null ? total : 0);
        return page;
    }

    @Override
    public String createDoctor(SysUser doctor, String community) {
        // 1. 校验手机号唯一
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getPhone, doctor.getPhone());
        if (this.baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该手机号已被注册");
        }

        // 2. 创建用户
        doctor.setUsername(doctor.getPhone());           // 用户名=手机号
        doctor.setCommunity(community);
        doctor.setStatus(1);
        String initPwd = "123456";
        doctor.setPassword(passwordEncoder.encode(initPwd));
        this.baseMapper.insert(doctor);

        // 3. 分配医生角色
        SysUserRole ur = new SysUserRole();
        ur.setUserId(doctor.getId());
        ur.setRoleId(DOCTOR_ROLE_ID);
        sysUserRoleMapper.insert(ur);

        return initPwd;
    }

    @Override
    public void updateDoctor(SysUser doctor) {
        // 只更新允许修改的字段：姓名/邮箱/性别
        SysUser db = this.baseMapper.selectById(doctor.getId());
        if (db == null) throw new RuntimeException("医生不存在");
        db.setRealName(doctor.getRealName());
        db.setEmail(doctor.getEmail());
        db.setGender(doctor.getGender());
        this.baseMapper.updateById(db);
    }

    @Override
    public void toggleDoctorStatus(Long doctorId, Integer status) {
        SysUser doctor = this.baseMapper.selectById(doctorId);
        if (doctor == null) throw new RuntimeException("医生不存在");

        doctor.setStatus(status);
        this.baseMapper.updateById(doctor);

        if (status == 0) {
            // 禁用：将签约老人的 doctor_id 置空（回到待分配）
            jdbcTemplate.update(
                "UPDATE elderly SET doctor_id = NULL WHERE doctor_id = ? AND is_deleted = 0",
                doctorId);
            // 关闭该医生负责的待处理预警（跨域调用 C 的 WarningRecordService）
            warningRecordService.closePendingByDoctorId(doctorId);
        }
    }

    @Override
    public Long countSigningElderly(Long doctorId) {
        String sql = "SELECT COUNT(*) FROM elderly WHERE doctor_id = ? AND is_deleted = 0";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, doctorId);
        return count != null ? count : 0;
    }

    // ========== 供 B/C 跨域调用 ==========

    @Override
    public SysUser getById(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public List<SysUser> listDoctorsByCommunity(String community) {
        // 只返回角色=DOCTOR的用户，需要联表过滤
        String sql = "SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id AND ur.role_id = ? " +
            "WHERE u.community = ? AND u.status = 1 AND u.is_deleted = 0";
        return jdbcTemplate.query(sql,
            (rs, rowNum) -> {
                SysUser u = new SysUser();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setRealName(rs.getString("real_name"));
                u.setPhone(rs.getString("phone"));
                u.setGender(rs.getInt("gender"));
                u.setCommunity(rs.getString("community"));
                u.setStatus(rs.getInt("status"));
                return u;
            },
            DOCTOR_ROLE_ID, community);
    }
}
