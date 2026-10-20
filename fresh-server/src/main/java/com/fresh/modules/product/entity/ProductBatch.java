package com.fresh.modules.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商品批次实体（保质期/临期/损耗核心）
 */
@Data
@TableName("product_batch")
public class ProductBatch implements Serializable {

    /** 状态-正常 */
    public static final Integer STATUS_NORMAL = 1;
    /** 状态-临期 */
    public static final Integer STATUS_NEAR_EXPIRY = 2;
    /** 状态-过期 */
    public static final Integer STATUS_EXPIRED = 3;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品id */
    private Long productId;

    /** 批次号 */
    private String batchNo;

    /** 生产日期 */
    private LocalDate productionDate;

    /** 到期日期 */
    private LocalDate expiryDate;

    /** 批次剩余库存 */
    private Integer stock;

    /** 批次初始库存 */
    private Integer initStock;

    /** 折扣(临期自动设置, 如0.70=7折) */
    private BigDecimal discount;

    /** 状态 1正常 2临期 3过期 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;
}
