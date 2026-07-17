package com.swjtu.smec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swjtu.smec.entity.DeviceMonitorLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 设备监控日志 Mapper
 *
 * @author C
 */
@Mapper
public interface DeviceMonitorLogMapper extends BaseMapper<DeviceMonitorLog> {

    /**
     * 查询某设备最近 N 条日志
     */
    @Select("SELECT * FROM device_monitor_log WHERE device_id = #{deviceId} ORDER BY create_time DESC LIMIT #{limit}")
    List<DeviceMonitorLog> selectByDeviceId(@Param("deviceId") Long deviceId, @Param("limit") int limit);

    /**
     * 查询某设备指定时间范围的心跳记录
     */
    @Select("SELECT * FROM device_monitor_log WHERE device_id = #{deviceId} " +
            "AND event_type IN (1, 2) " +
            "AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY create_time ASC")
    List<DeviceMonitorLog> selectHeartbeats(
            @Param("deviceId") Long deviceId,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );
}
