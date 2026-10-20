package com.fresh.modules.cart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体（支持按份/按重量）
 */
@Data
@TableName("shopping_cart")
public class ShoppingCart implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称(冗余) */
    private String name;

    /** 商品图片(冗余) */
    private String image;

    /** 用户id */
    private Long userId;

    /** 商品id */
    private Long productId;

    /** 称重重量(按重量商品) */
    private BigDecimal weight;

    /** 份数(按份商品) */
    private Integer number;

    /** 小计金额 */
    private BigDecimal amount;

    /** 加入时间 */
    private LocalDateTime createTime;
}
