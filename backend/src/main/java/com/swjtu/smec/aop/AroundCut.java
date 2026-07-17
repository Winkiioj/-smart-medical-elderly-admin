package com.swjtu.smec.aop;

import com.swjtu.smec.common.enums.GlobalErrorCodeConstants;
import com.swjtu.smec.common.result.CommonResult;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * AOP 切面 — Token 校验
 *
 * <p>拦截所有 Controller 方法，检查请求头中的 Token 是否有效。
 * 标记了 @NoToken 的方法跳过校验。</p>
 */
@Component
@Aspect
public class AroundCut {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public static final String POINT_CUT =
            "execution(* com.swjtu.smec.controller.*.*(..))";

    @Around(POINT_CUT)
    public Object checkToken(ProceedingJoinPoint pjp) throws Throwable {
        // 1. 检查方法是否有 @NoToken 注解
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        if (method.isAnnotationPresent(com.swjtu.smec.common.annotation.NoToken.class)) {
            return pjp.proceed();
        }

        // 2. 获取请求头中的 Token
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return CommonResult.error(
                    GlobalErrorCodeConstants.UNAUTHORIZED.getCode(),
                    GlobalErrorCodeConstants.UNAUTHORIZED.getMsg()
            );
        }
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("Token");

        // 3. 去 Redis 中验证 Token
        if (StringUtils.isBlank(token)) {
            return CommonResult.error(
                    GlobalErrorCodeConstants.UNAUTHORIZED.getCode(),
                    "Token 不能为空"
            );
        }

        String userInfo = redisTemplate.opsForValue().get("Token::" + token);
        if (StringUtils.isBlank(userInfo)) {
            return CommonResult.error(
                    GlobalErrorCodeConstants.UNAUTHORIZED.getCode(),
                    "Token 已失效，请重新登录"
            );
        }

        // 4. Token 有效，继续执行
        return pjp.proceed();
    }
}
