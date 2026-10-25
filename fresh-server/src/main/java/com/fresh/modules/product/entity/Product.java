package com.fresh.modules.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生鲜商品实体
 */
@Data
@TableName("product")
public class Product implements Serializable {

    /** 计价方式-按份 */
    public static final Integer PRICE_TYPE_PER = 1;
    /** 计价方式-按重量 */
    public static final Integer PRICE_TYPE_WEIGHT = 2;

    /** 保鲜类型-常温 */
    public static final Integer FRESH_NORMAL = 0;
    /** 保鲜类型-冷藏 */
    public static final Integer FRESH_COLD = 1;
    /** 保鲜类型-冷冻 */
    public static final Integer FRESH_FROZEN = 2;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 分类id */
    private Long categoryId;

    /** 单价(每份/每斤/每kg) */
    private BigDecimal price;

    /** 计价方式 1按份 2按重量 */
    private Integer priceType;

    /** 计价单位: 份/斤/kg */
    private String unit;

    /** 保鲜类型 0常温 1冷藏 2冷冻 */
    private Integer freshType;

    /** 保质期(天) */
    private Integer shelfLifeDays;

    /** 总库存 */
    private Integer stock;

    /** 预警库存 */
    private Integer warnStock;

    /** 产地 */
    private String originPlace;

    /** 溯源码 */
    private String traceCode;

    /** 商品图片 */
    private String image;

    /** 商品描述 */
    private String description;

    /** 是否预售 0否 1是 */
    private Integer isPresale;

    /** 销量(展示用) */
    private Integer saleCount;

    /** 状态 1起售 0停售 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;

    /** 创建人 */
    private Long createUser;

    /** 修改人 */
    private Long updateUser;
}
