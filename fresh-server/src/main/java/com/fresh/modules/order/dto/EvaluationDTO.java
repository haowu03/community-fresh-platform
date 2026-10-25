package com.fresh.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品评价入参
 */
@Data
@Schema(description = "商品评价入参")
public class EvaluationDTO implements Serializable {

    /** 订单id */
    @Schema(description = "订单id")
    private Long orderId;

    /** 商品id */
    @Schema(description = "商品id")
    private Long productId;

    /** 评分 1-5 */
    @Schema(description = "评分 1-5")
    private Integer score;

    /** 评价内容 */
    @Schema(description = "评价内容")
    private String content;

    /** 评价图片(JSON数组) */
    @Schema(description = "评价图片(JSON数组)")
    private String images;
}
