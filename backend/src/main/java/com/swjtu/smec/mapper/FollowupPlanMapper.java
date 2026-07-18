package com.swjtu.smec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swjtu.smec.entity.FollowupPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FollowupPlanMapper extends BaseMapper<FollowupPlan> {
    IPage<FollowupPlan> selectPageByDoctor(Page<FollowupPlan> page,
                                           @Param("doctorId") Long doctorId,
                                           @Param("followupType") Integer followupType,
                                           @Param("status") Integer status,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);
}
