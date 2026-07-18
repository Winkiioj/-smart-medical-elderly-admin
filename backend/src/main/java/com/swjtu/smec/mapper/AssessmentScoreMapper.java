package com.swjtu.smec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swjtu.smec.entity.AssessmentScore;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AssessmentScoreMapper extends BaseMapper<AssessmentScore> {
    @Select("SELECT * FROM assessment_score WHERE report_id = #{reportId}")
    List<AssessmentScore> selectByReportId(@Param("reportId") Long reportId);

    @Delete("DELETE FROM assessment_score WHERE report_id = #{reportId}")
    int deleteByReportId(@Param("reportId") Long reportId);
}
