package com.swjtu.smec.common.annotation;

import java.lang.annotation.*;

/**
 * 免登录注解 — 标记在 Controller 方法上，跳过 Token 校验
 *
 * <pre>
 * 用法：
 *   @NoToken
 *   @PostMapping("/login")
 *   public CommonResult login(...) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoToken {
}
