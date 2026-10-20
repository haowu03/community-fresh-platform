package com.fresh.modules.category.controller;

import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.category.dto.CategoryDTO;
import com.fresh.modules.category.dto.CategoryPageQueryDTO;
import com.fresh.modules.category.entity.Category;
import com.fresh.modules.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-商品分类管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
@Tag(name = "管理端-分类管理")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 分类分页查询
     */
    @GetMapping("/page")
    @Operation(summary = "分类分页查询")
    public R<PageResult> page(CategoryPageQueryDTO queryDTO) {
        log.info("分类分页查询：{}", queryDTO);
        return R.success(categoryService.pageQuery(queryDTO));
    }

    /**
     * 新增分类
     */
    @PostMapping
    @Operation(summary = "新增分类")
    public R<Void> save(@RequestBody CategoryDTO categoryDTO) {
        log.info("新增分类：{}", categoryDTO);
        categoryService.saveCategory(categoryDTO);
        return R.success();
    }

    /**
     * 修改分类
     */
    @PutMapping
    @Operation(summary = "修改分类")
    public R<Void> update(@RequestBody CategoryDTO categoryDTO) {
        log.info("修改分类：{}", categoryDTO);
        categoryService.updateCategory(categoryDTO);
        return R.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping
    @Operation(summary = "删除分类")
    public R<Void> delete(@RequestParam Long id) {
        log.info("删除分类：{}", id);
        categoryService.deleteById(id);
        return R.success();
    }

    /**
     * 启用/禁用分类
     */
    @PutMapping("/status/{status}")
    @Operation(summary = "启用禁用分类")
    public R<Void> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("分类启停：status={}, id={}", status, id);
        categoryService.startOrStop(status, id);
        return R.success();
    }

    /**
     * 查询启用分类列表（管理端下拉用）
     */
    @GetMapping("/list")
    @Operation(summary = "启用分类列表")
    public R<List<Category>> list() {
        return R.success(categoryService.listEnabled());
    }
}
