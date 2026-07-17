package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.entity.FamilyContact;

import java.util.List;

/**
 * 家属联系人 Service
 */
public interface FamilyContactService extends IService<FamilyContact> {

    /**
     * 查某老人所有家属
     */
    List<FamilyContact> listByElderlyId(Long elderlyId);

    /**
     * 编辑老人时批量更新家属（先删后插，同一事务）
     */
    void replaceByElderlyId(Long elderlyId, List<FamilyContact> contacts);
}
