package com.swjtu.smec.controller;

import com.swjtu.smec.common.annotation.NoToken;
import com.swjtu.smec.common.result.CommonResult;
import com.swjtu.smec.entity.ElderlyTag;
import com.swjtu.smec.service.ElderlyTagMappingService;
import com.swjtu.smec.service.ElderlyTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 老人标签 Controller
 *
 * @author C
 */
@RestController
@RequestMapping("/api/tag")
public class ElderlyTagController {

    @Autowired
    private ElderlyTagService tagService;

    @Autowired
    private ElderlyTagMappingService mappingService;

    // TODO: A 完成登录认证后删除所有 @NoToken

    @NoToken
    @GetMapping("/list")
    public CommonResult list() {
        return tagService.listAll();
    }

    @NoToken
    @PostMapping
    public CommonResult add(@RequestBody ElderlyTag tag) {
        return tagService.add(tag);
    }

    @NoToken
    @PutMapping
    public CommonResult update(@RequestBody ElderlyTag tag) {
        return tagService.update(tag);
    }

    @NoToken
    @DeleteMapping("/{id}")
    public CommonResult delete(@PathVariable Long id) {
        return tagService.delete(id);
    }

    @NoToken
    @GetMapping("/elderly/{elderlyId}")
    public CommonResult getByElderly(@PathVariable Long elderlyId) {
        return mappingService.getTagIdsByElderly(elderlyId);
    }

    @NoToken
    @PutMapping("/elderly/{elderlyId}")
    public CommonResult saveByElderly(@PathVariable Long elderlyId,
                                      @RequestBody List<Long> tagIds) {
        return mappingService.saveTags(elderlyId, tagIds);
    }
}
