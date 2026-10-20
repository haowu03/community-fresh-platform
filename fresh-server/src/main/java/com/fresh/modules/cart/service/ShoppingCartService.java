package com.fresh.modules.cart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.modules.cart.dto.ShoppingCartDTO;
import com.fresh.modules.cart.entity.ShoppingCart;

import java.util.List;

/**
 * 购物车 Service
 */
public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * 加入购物车（按份/按重量，同商品同重量规格已存在则累加）
     *
     * @param shoppingCartDTO 加购信息
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 减少一份/删除购物车条目
     *
     * @param shoppingCartDTO 减购信息
     */
    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看当前用户购物车
     *
     * @return 购物车列表
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清空当前用户购物车
     */
    void cleanShoppingCart();
}
