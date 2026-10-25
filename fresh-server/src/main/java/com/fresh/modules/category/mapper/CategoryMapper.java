package com.fresh.modules.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.modules.category.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类 Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
