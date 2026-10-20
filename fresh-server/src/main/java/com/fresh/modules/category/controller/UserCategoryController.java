package com.fresh.modules.category.controller;

import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.common.result.R;
import com.fresh.modules.category.entity.Category;
import com.fresh.modules.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端-商品分类浏览
 */
@Slf4j
@RestController
@RequestMapping("/user/category")
@RequiredArgsConstructor
@Tag(name = "用户端-分类浏览")
public class UserCategoryController {

    private final CategoryService categoryService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询启用分类列表（带 Redis 缓存）
     */
    @GetMapping("/list")
    @Operation(summary = "启用分类列表")
    @SuppressWarnings("unchecked")
    public R<List<Category>> list() {
        // 先查缓存，命中直接返回
        Object cached = redisTemplate.opsForValue().get(RedisCacheConstant.CATEGORY_LIST);
        if (cached != null) {
            return R.success((List<Category>) cached);
        }
        List<Category> list = categoryService.listEnabled();
        redisTemplate.opsForValue().set(RedisCacheConstant.CATEGORY_LIST, list);
        return R.success(list);
    }
}
