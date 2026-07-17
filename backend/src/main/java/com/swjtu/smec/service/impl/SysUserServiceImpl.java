package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.entity.SysUser;
import com.swjtu.smec.mapper.SysUserMapper;
import com.swjtu.smec.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private static final Duration TOKEN_TTL = Duration.ofHours(8);

    @Override
    public String login(String username, String password) {
        // 1. 查用户
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser user = this.baseMapper.selectOne(wrapper);

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 2. 验密码（数据库中可能是明文或 BCrypt，兼容两种）
        boolean matches;
        if (user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$")) {
            matches = passwordEncoder.matches(password, user.getPassword());
        } else {
            matches = password.equals(user.getPassword());
        }

        if (!matches) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 生成 Token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 4. 存 Redis：key = Token::<token>, value = 用户JSON
        String userJson = "{\"userId\":" + user.getId()
                + ",\"username\":\"" + user.getUsername()
                + "\",\"realName\":\"" + (user.getRealName() != null ? user.getRealName() : "")
                + "\",\"community\":\"" + (user.getCommunity() != null ? user.getCommunity() : "")
                + "\"}";
        redisTemplate.opsForValue().set("Token::" + token, userJson, TOKEN_TTL);

        // 5. 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        this.baseMapper.updateById(user);

        return token;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete("Token::" + token);
    }

    @Override
    public SysUser getById(Long id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public List<SysUser> listDoctorsByCommunity(String community) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getCommunity, community)
               .eq(SysUser::getStatus, 1);
        // 注意：医生角色的筛选需要通过 sys_user_role 联表，
        // 这里先按 community 过滤，前端/调用方再结合角色信息筛选
        return this.baseMapper.selectList(wrapper);
    }
}
