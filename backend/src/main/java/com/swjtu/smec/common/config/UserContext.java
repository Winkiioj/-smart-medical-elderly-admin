package com.swjtu.smec.common.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 获取当前登录用户信息
 */
@Component
public class UserContext {

    private static StringRedisTemplate redis;

    @Autowired
    public void setRedis(StringRedisTemplate redisTemplate) {
        UserContext.redis = redisTemplate;
    }

    /**
     * 从请求头 Token 中解析当前用户信息
     */
    public static JSONObject currentUser() {
        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) return null;
        HttpServletRequest req = attr.getRequest();
        String token = req.getHeader("Token");
        if (token == null) return null;
        String json = redis.opsForValue().get("Token::" + token);
        if (json == null) return null;
        return JSON.parseObject(json);
    }

    /** 当前用户ID */
    public static Long currentUserId() {
        JSONObject user = currentUser();
        return user != null ? user.getLong("userId") : null;
    }

    /** 当前用户所属社区 */
    public static String currentCommunity() {
        JSONObject user = currentUser();
        return user != null ? user.getString("community") : null;
    }
}
