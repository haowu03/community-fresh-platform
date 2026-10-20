package com.fresh.modules.cart.controller;

import com.fresh.common.result.R;
import com.fresh.modules.cart.dto.ShoppingCartDTO;
import com.fresh.modules.cart.entity.ShoppingCart;
import com.fresh.modules.cart.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端-购物车
 */
@Slf4j
@RestController
@RequestMapping("/user/shoppingCart")
@RequiredArgsConstructor
@Tag(name = "用户端-购物车")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    /**
     * 加入购物车
     */
    @PostMapping("/add")
    @Operation(summary = "加入购物车")
    public R<Void> add(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("加入购物车：{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return R.success();
    }

    /**
     * 减少一份/删除
     */
    @PostMapping("/sub")
    @Operation(summary = "减少一份/删除")
    public R<Void> sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车减购：{}", shoppingCartDTO);
        shoppingCartService.subShoppingCart(shoppingCartDTO);
        return R.success();
    }

    /**
     * 查看当前用户购物车
     */
    @GetMapping("/list")
    @Operation(summary = "查看购物车")
    public R<List<ShoppingCart>> list() {
        return R.success(shoppingCartService.showShoppingCart());
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    @Operation(summary = "清空购物车")
    public R<Void> clean() {
        shoppingCartService.cleanShoppingCart();
        return R.success();
    }
}
