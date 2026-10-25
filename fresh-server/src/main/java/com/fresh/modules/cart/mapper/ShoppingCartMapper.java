package com.fresh.modules.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.modules.cart.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车 Mapper
 */
@Mapper
public interface ShoppingCartMapper extends BaseMapper<ShoppingCart> {
}
