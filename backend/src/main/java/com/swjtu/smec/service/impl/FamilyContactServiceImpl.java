package com.swjtu.smec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swjtu.smec.entity.FamilyContact;
import com.swjtu.smec.mapper.FamilyContactMapper;
import com.swjtu.smec.service.FamilyContactService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FamilyContactServiceImpl extends ServiceImpl<FamilyContactMapper, FamilyContact>
        implements FamilyContactService {

    @Override
    public List<FamilyContact> listByElderlyId(Long elderlyId) {
        LambdaQueryWrapper<FamilyContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyContact::getElderlyId, elderlyId);
        return this.baseMapper.selectList(wrapper);
    }

    @Override
    public void replaceByElderlyId(Long elderlyId, List<FamilyContact> contacts) {
        // 先删
        LambdaQueryWrapper<FamilyContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FamilyContact::getElderlyId, elderlyId);
        this.baseMapper.delete(wrapper);

        // 后插
        if (contacts != null && !contacts.isEmpty()) {
            for (FamilyContact c : contacts) {
                c.setElderlyId(elderlyId);
            }
            this.saveBatch(contacts);
        }
    }
}
