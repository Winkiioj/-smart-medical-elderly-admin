package com.swjtu.smec.controller;

import com.swjtu.smec.common.annotation.NoToken;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.service.WarningRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 预警处理 Controller
 *
 * @author C
 */
@RestController
@RequestMapping("/api/warning")
public class WarningController {

    @Autowired
    private WarningRecordService warningService;

    // TODO: A 完成登录认证后删除所有 @NoToken

    @NoToken
    @GetMapping("/page")
    public CommonResult page(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Integer alertLevel,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return warningService.pageByDoctor(pageNo, pageSize, doctorId,
                alertLevel, alertType, status, startTime, endTime);
    }

    @NoToken
    @GetMapping("/detail/{id}")
    public CommonResult detail(@PathVariable Long id) {
        return warningService.getDetailWithElderly(id);
    }

    @NoToken
    @PostMapping("/accept/{id}")
    public CommonResult accept(@PathVariable Long id, @RequestParam Long handlerId) {
        return warningService.accept(id, handlerId);
    }

    @NoToken
    @PostMapping("/complete/{id}")
    public CommonResult complete(@PathVariable Long id,
                                  @RequestParam String opinion,
                                  @RequestParam String result) {
        return warningService.complete(id, opinion, result);
    }

    @NoToken
    @PostMapping("/close/{id}")
    public CommonResult close(@PathVariable Long id, @RequestParam String reason) {
        return warningService.close(id, reason);
    }
}
