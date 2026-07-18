package com.swjtu.smec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swjtu.smec.entity.FollowupRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowupRecordMapper extends BaseMapper<FollowupRecord> {
    List<FollowupRecord> selectRecentByElderly(@Param("elderlyId") Long elderlyId, @Param("limit") int limit);
}
