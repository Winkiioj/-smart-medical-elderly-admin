package com.swjtu.smec.service;

import java.util.List;
import java.util.Map;

public interface AssignService {

    // 待分配老人列表
    Map<String, Object> listUnassigned(String community, int pageNo, int pageSize, String keyword);

    // 已分配老人列表
    Map<String, Object> listAssigned(String community, int pageNo, int pageSize,
                                      Long doctorId, String keyword);

    // 医生负荷
    List<Map<String, Object>> getDoctorLoad(String community);

    // 分配医生
    void assignDoctor(Long elderlyId, Long doctorId);

    // 批量分配
    int batchAssign(List<Long> elderlyIds, Long doctorId);

    // 调整分配
    void reassignDoctor(Long elderlyId, Long newDoctorId);
}
