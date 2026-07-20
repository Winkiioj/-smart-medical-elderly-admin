package com.swjtu.smec.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.entity.Elderly;

import java.time.LocalDate;

/**
 * 老人档案 Service
 */
public interface ElderlyService extends IService<Elderly> {

    /**
     * 分页查询（按社区 + 姓名/身份证 + 年龄段筛选）
     */
    IPage<Elderly> page(Long doctorId, String keyword, String community,
                        Integer gender, Integer ageMin, Integer ageMax,
                        Integer status, Long tagId,
                        LocalDate startDate, LocalDate endDate,
                        int page, int size);

    /**
     * [供 C 跨域调用] 根据ID查老人
     */
    Elderly getById(Long id);

    /**
     * [供 C 跨域调用] 查某医生的所有签约老人列表
     */
    java.util.List<Elderly> listByDoctorId(Long doctorId);
}
