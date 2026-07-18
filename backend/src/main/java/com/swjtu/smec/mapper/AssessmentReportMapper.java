package com.swjtu.smec.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swjtu.smec.entity.AssessmentReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssessmentReportMapper extends BaseMapper<AssessmentReport> {
    @Select("SELECT * FROM assessment_report WHERE elderly_id = #{elderlyId} AND template_id = #{templateId} AND status = 1 AND create_time >= DATE_SUB(NOW(), INTERVAL 1 MONTH) ORDER BY create_time DESC LIMIT 1")
    AssessmentReport selectLatestPublished(@Param("elderlyId") Long elderlyId,
                                          @Param("templateId") Long templateId);
}
