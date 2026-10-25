package com.fresh.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.modules.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生鲜商品 Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
