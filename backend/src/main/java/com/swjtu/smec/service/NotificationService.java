package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Notification;

/**
 * 消息通知 Service — A 负责
 *
 * @author A
 */
public interface NotificationService extends IService<Notification> {

    /** 分页查询当前用户的通知 */
    CommonResult page(int pageNo, int pageSize);

    /** 未读数量 */
    CommonResult unreadCount();

    /** 标记单条已读 */
    CommonResult markRead(Long id);

    /** 全部标记已读 */
    CommonResult markAllRead();

    // ===== 供 B/C 跨域调用 =====

    /**
     * 创建通知（供预警/随访/建档等触发）
     */
    void create(Long userId, String title, String content, Integer type);

    /**
     * [供 B/C 跨域调用] 查某用户的未读消息数
     */
    int countUnreadByUserId(Long userId);
}
