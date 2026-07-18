package com.swjtu.smec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swjtu.smec.entity.AssessmentDimension;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssessmentDimensionMapper extends BaseMapper<AssessmentDimension> {
    @Select("SELECT * FROM assessment_dimension WHERE template_id = #{templateId} ORDER BY sort_order")
    List<AssessmentDimension> selectByTemplateId(@Param("templateId") Long templateId);
}
