package com.swjtu.smec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swjtu.smec.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 设备台账 Mapper
 *
 * @author C
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    /**
     * 分页条件查询设备列表
     */
    IPage<Device> selectPage(
            Page<Device> page,
            @Param("keyword") String keyword,
            @Param("deviceType") String deviceType,
            @Param("status") Integer status,
            @Param("community") String community
    );

    /**
     * 按设备编号查询（查重用）
     */
    @Select("SELECT * FROM device WHERE device_no = #{deviceNo}")
    Device selectByDeviceNo(@Param("deviceNo") String deviceNo);

    /**
     * 按状态统计数量
     */
    @Select("SELECT COUNT(*) FROM device WHERE status = #{status}")
    int countByStatus(@Param("status") Integer status);

}
