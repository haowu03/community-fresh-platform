package com.fresh.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细实体
 */
@Data
@TableName("order_detail")
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称(冗余) */
    private String name;

    /** 商品图片(冗余) */
    private String image;

    /** 订单id */
    private Long orderId;

    /** 商品id */
    private Long productId;

    /** 扣减批次id */
    private Long batchId;

    /** 称重重量(按重量商品) */
    private BigDecimal weight;

    /** 份数 */
    private Integer number;

    /** 小计金额 */
    private BigDecimal amount;
}
