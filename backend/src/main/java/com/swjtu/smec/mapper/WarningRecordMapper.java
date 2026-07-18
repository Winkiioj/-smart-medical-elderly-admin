package com.swjtu.smec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swjtu.smec.entity.WarningRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 预警记录 Mapper
 *
 * @author C
 */
@Mapper
public interface WarningRecordMapper extends BaseMapper<WarningRecord> {

    /**
     * 分页查询（按老人ID列表过滤 + 条件筛选）
     */
    IPage<WarningRecord> selectPageByDoctor(Page<WarningRecord> page,
                                            @Param("elderlyIds") java.util.List<Long> elderlyIds,
                                            @Param("alertLevel") Integer alertLevel,
                                            @Param("alertType") String alertType,
                                            @Param("status") Integer status,
                                            @Param("startTime") String startTime,
                                            @Param("endTime") String endTime);

    /** 统计待处理预警数 */
    @Select("SELECT COUNT(*) FROM warning_record WHERE status = 0")
    int countPending();

    /** 24小时内同老人+同指标去重 */
    @Select("SELECT COUNT(*) FROM warning_record WHERE elderly_id = #{elderlyId} AND alert_type = #{alertType} AND create_time > #{since}")
    int countWithin24h(@Param("elderlyId") Long elderlyId,
                       @Param("alertType") String alertType,
                       @Param("since") String since);
}

