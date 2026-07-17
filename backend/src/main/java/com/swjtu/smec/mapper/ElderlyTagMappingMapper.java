package com.swjtu.smec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swjtu.smec.entity.ElderlyTagMapping;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 老人标签关联 Mapper
 *
 * @author C
 */
@Mapper
public interface ElderlyTagMappingMapper extends BaseMapper<ElderlyTagMapping> {

    /**
     * 查询某老人的所有标签
     */
    @Select("SELECT * FROM elderly_tag_mapping WHERE elderly_id = #{elderlyId}")
    List<ElderlyTagMapping> selectByElderlyId(@Param("elderlyId") Long elderlyId);

    /**
     * 删除某老人的所有标签关联（编辑时先删后插）
     */
    @Delete("DELETE FROM elderly_tag_mapping WHERE elderly_id = #{elderlyId}")
    int deleteByElderlyId(@Param("elderlyId") Long elderlyId);
}
