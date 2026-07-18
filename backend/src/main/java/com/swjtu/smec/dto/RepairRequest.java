package com.swjtu.smec.dto;

import lombok.Data;

/**
 * 设备报修请求 DTO
 */
@Data
public class RepairRequest {
    private Long deviceId;
    private String faultType;
    private String faultDescription;
    private Long reporterId;
    private String reporterName;
}
