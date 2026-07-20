package com.swjtu.smec.controller;

import com.swjtu.smec.common.annotation.NoToken;
import com.swjtu.smec.dto.RepairRequest;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Device;
import com.swjtu.smec.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 设备台账 Controller
 *
 * @author C
 */
@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    // TODO: A 完成登录认证后删除所有 @NoToken，恢复 AOP Token 校验

    @NoToken
    @GetMapping("/page")
    public CommonResult page(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String community) {
        return deviceService.pageList(pageNo, pageSize, keyword, deviceType, status, community);
    }

    @NoToken
    @GetMapping("/detail/{id}")
    public CommonResult detail(@PathVariable Long id) {
        return deviceService.getDetail(id);
    }

    @NoToken
    @PostMapping("/add")
    public CommonResult add(@RequestBody Device device,
                            @RequestParam Long operatorId) {
        return deviceService.add(device, operatorId);
    }

    @NoToken
    @PutMapping("/update")
    public CommonResult update(@RequestBody Device device) {
        return deviceService.updateInfo(device);
    }

    @NoToken
    @PutMapping("/status/{id}")
    public CommonResult updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status,
                                     @RequestParam Long operatorId) {
        return deviceService.updateStatus(id, status, operatorId);
    }

    @NoToken
    @PostMapping("/repair")
    public CommonResult submitRepair(@RequestBody RepairRequest req) {
        return deviceService.submitRepair(
                req.getDeviceId(), req.getFaultType(), req.getFaultDescription(),
                req.getReporterId(), req.getReporterName());
    }

    @NoToken
    @GetMapping("/dashboard")
    public CommonResult dashboard(@RequestParam(defaultValue = "1") int repairPageNo,
                                  @RequestParam(defaultValue = "8") int repairPageSize) {
        return deviceService.getDashboard(repairPageNo, repairPageSize);
    }

    @NoToken
    @GetMapping("/stats")
    public CommonResult stats() {
        return deviceService.getStats();
    }

    @NoToken
    @GetMapping("/repair-records")
    public CommonResult repairRecords() {
        return deviceService.getRepairRecords();
    }
}
