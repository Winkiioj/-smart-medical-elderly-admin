package com.swjtu.smec.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.Community;

import java.util.List;

/**
 * 社区管理 Service — A 负责
 *
 * @author A
 */
public interface CommunityService extends IService<Community> {

    /** 分页列表 */
    CommonResult page(int pageNo, int pageSize, String keyword);

    /** 新增 */
    CommonResult add(Community community);

    /** 编辑 */
    CommonResult update(Community community);

    /** 停用/启用 */
    CommonResult toggleStatus(Long id, Integer status);

    /** 启用列表（供前端下拉/其他域引用） */
    CommonResult listEnabled();

    // ===== 供 B/C 跨域调用 =====

    /** 按名称查社区 */
    Community getByName(String name);

    /** 所有启用社区 */
    List<Community> listAllEnabled();
}
