package com.fresh.modules.category.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.common.constant.StatusConstant;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.common.result.PageResult;
import com.fresh.modules.category.dto.CategoryDTO;
import com.fresh.modules.category.dto.CategoryPageQueryDTO;
import com.fresh.modules.category.entity.Category;
import com.fresh.modules.category.mapper.CategoryMapper;
import com.fresh.modules.category.service.CategoryService;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO queryDTO) {
        Page<Category> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .like(StringUtils.hasText(queryDTO.getName()), Category::getName, queryDTO.getName())
                .orderByAsc(Category::getSort);
        this.page(page, wrapper);
        return new PageResult(page.getTotal(), page.getRecords());
    }

    @Override
    public void saveCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setStatus(StatusConstant.ENABLE);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());
        this.save(category);
        clearCache();
    }

    @Override
    public void updateCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO, category);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        this.updateById(category);
        clearCache();
    }

    @Override
    public void deleteById(Long id) {
        // 分类下有商品时禁止删除
        Long count = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, id));
        if (count != null && count > 0) {
            throw new BusinessException("该分类下存在商品，无法删除");
        }
        this.removeById(id);
        clearCache();
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        category.setUpdateTime(LocalDateTime.now());
        category.setUpdateUser(BaseContext.getCurrentId());
        this.updateById(category);
        clearCache();
    }

    @Override
    public List<Category> listEnabled() {
        return this.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, StatusConstant.ENABLE)
                .orderByAsc(Category::getSort));
    }

    /**
     * 分类变更后清理分类列表缓存
     */
    private void clearCache() {
        redisTemplate.delete(RedisCacheConstant.CATEGORY_LIST);
    }
}
