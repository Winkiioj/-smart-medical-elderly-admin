package com.swjtu.smec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swjtu.smec.entity.Community;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 社区 Mapper — A 负责
 *
 * @author A
 */
@Mapper
public interface CommunityMapper extends BaseMapper<Community> {

    /** 所有启用社区（下拉列表用） */
    @Select("SELECT * FROM community WHERE status = 1 ORDER BY id")
    List<Community> listEnabled();

    /** 按名称查（重名校验） */
    @Select("SELECT * FROM community WHERE name = #{name} LIMIT 1")
    Community selectByName(@Param("name") String name);
}
