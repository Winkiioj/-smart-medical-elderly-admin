package com.swjtu.smec.controller;

import com.swjtu.smec.common.annotation.NoToken;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.common.util.CaptchaUtil;
import com.swjtu.smec.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 登录控制器
 */
@Tag(name = "认证", description = "登录/退出/验证码")
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Duration CAPTCHA_TTL = Duration.ofMinutes(5);
    private static final String CAPTCHA_PREFIX = "Captcha::";

    /**
     * 获取图形验证码
     */
    @NoToken
    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public CommonResult<Map<String, String>> captcha() {
        CaptchaUtil.CaptchaResult result = CaptchaUtil.generate();
        String key = UUID.randomUUID().toString().substring(0, 8);

        // 存 Redis：key → 验证码，5 分钟有效
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + key, result.code, CAPTCHA_TTL);

        Map<String, String> data = new HashMap<>();
        data.put("captchaKey", key);
        data.put("captchaImage", result.image);
        return CommonResult.success(data);
    }

    /**
     * 登录
     */
    @NoToken
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public CommonResult<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String captchaKey = body.get("captchaKey");
        String captchaCode = body.get("captchaCode");

        if (username == null || password == null) {
            return CommonResult.error(400, "用户名和密码不能为空");
        }

        // 验证码校验
        if (captchaKey == null || captchaCode == null) {
            return CommonResult.error(400, "请输入验证码");
        }
        String cachedCode = redisTemplate.opsForValue().get(CAPTCHA_PREFIX + captchaKey);
        if (cachedCode == null) {
            return CommonResult.error(400, "验证码已过期，请刷新后重试");
        }
        if (!cachedCode.equalsIgnoreCase(captchaCode)) {
            return CommonResult.error(400, "验证码错误");
        }
        // 验证成功 → 立即删除，防止同一个 key 重复用
        redisTemplate.delete(CAPTCHA_PREFIX + captchaKey);

        try {
            Map<String, Object> loginResult = sysUserService.login(username, password);
            return CommonResult.success(loginResult);
        } catch (RuntimeException e) {
            return CommonResult.error(401, e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public CommonResult<?> logout(HttpServletRequest request) {
        String token = request.getHeader("Token");
        if (token != null && !token.isEmpty()) {
            sysUserService.logout(token);
        }
        return CommonResult.success(null);
    }
}
