package com.swjtu.smec.controller;

import com.swjtu.smec.common.annotation.NoToken;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 登录控制器
 */
@Tag(name = "认证", description = "登录/退出")
@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 登录
     */
    @NoToken
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public CommonResult<Map<String, String>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return CommonResult.error(400, "用户名和密码不能为空");
        }

        try {
            String token = sysUserService.login(username, password);
            Map<String, String> data = Map.of("token", token);
            return CommonResult.success(data);
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
