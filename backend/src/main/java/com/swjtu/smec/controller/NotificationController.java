package com.swjtu.smec.controller;

import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知 Controller — A 负责
 *
 * @author A
 */
@Tag(name = "消息通知", description = "通知列表/未读/已读")
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "通知列表（分页）")
    @GetMapping("/page")
    public CommonResult page(@RequestParam(defaultValue = "1") int pageNo,
                             @RequestParam(defaultValue = "20") int pageSize) {
        return notificationService.page(pageNo, pageSize);
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public CommonResult unreadCount() {
        return notificationService.unreadCount();
    }

    @Operation(summary = "标记已读")
    @PutMapping("/read/{id}")
    public CommonResult markRead(@PathVariable Long id) {
        return notificationService.markRead(id);
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/read-all")
    public CommonResult markAllRead() {
        return notificationService.markAllRead();
    }
}
