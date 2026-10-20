package com.fresh.modules.category.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.common.result.PageResult;
import com.fresh.modules.category.dto.CategoryDTO;
import com.fresh.modules.category.dto.CategoryPageQueryDTO;
import com.fresh.modules.category.entity.Category;

import java.util.List;

/**
 * 商品分类 Service
 */
public interface CategoryService extends IService<Category> {

    /**
     * 分类分页查询
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(CategoryPageQueryDTO queryDTO);

    /**
     * 新增分类
     *
     * @param categoryDTO 分类信息
     */
    void saveCategory(CategoryDTO categoryDTO);

    /**
     * 修改分类
     *
     * @param categoryDTO 分类信息
     */
    void updateCategory(CategoryDTO categoryDTO);

    /**
     * 删除分类（分类下有商品时禁止删除）
     *
     * @param id 分类id
     */
    void deleteById(Long id);

    /**
     * 启用/禁用分类
     *
     * @param status 状态
     * @param id     分类id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 查询启用中的分类列表（按 sort 升序）
     *
     * @return 启用分类列表
     */
    List<Category> listEnabled();
}
