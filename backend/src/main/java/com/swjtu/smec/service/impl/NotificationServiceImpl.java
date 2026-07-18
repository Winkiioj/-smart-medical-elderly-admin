package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.common.config.UserContext;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Notification;
import com.swjtu.smec.mapper.NotificationMapper;
import com.swjtu.smec.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 消息通知 Service 实现 — A 负责
 *
 * @author A
 */
@Service
@Transactional
public class NotificationServiceImpl
        extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    public CommonResult page(int pageNo, int pageSize) {
        Long userId = UserContext.currentUserId();
        if (userId == null) return CommonResult.error(401, "请先登录");

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
               .orderByDesc(Notification::getCreateTime);
        Page<Notification> page = this.baseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return CommonResult.success(page.getRecords(), page.getTotal());
    }

    @Override
    public CommonResult unreadCount() {
        Long userId = UserContext.currentUserId();
        if (userId == null) return CommonResult.error(401, "请先登录");

        return CommonResult.success(this.baseMapper.countUnread(userId));
    }

    @Override
    public CommonResult markRead(Long id) {
        Notification n = this.baseMapper.selectById(id);
        if (n == null) return CommonResult.error(404, "通知不存在");

        n.setIsRead(1);
        n.setReadTime(LocalDateTime.now());
        this.baseMapper.updateById(n);
        return CommonResult.success(null);
    }

    @Override
    public CommonResult markAllRead() {
        Long userId = UserContext.currentUserId();
        if (userId == null) return CommonResult.error(401, "请先登录");

        this.baseMapper.markAllRead(userId);
        return CommonResult.success(null);
    }

    // ===== 供 B/C 跨域调用 =====

    @Override
    public int countUnreadByUserId(Long userId) {
        return this.baseMapper.countUnread(userId);
    }

    @Override
    public void create(Long userId, String title, String content, Integer type) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type != null ? type : 3); // 默认系统通知
        n.setIsRead(0);
        this.baseMapper.insert(n);
    }
}
