package com.fresh.modules.presale.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预售活动展示VO
 */
@Data
@Schema(description = "预售活动展示VO")
public class PresaleActivityVO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "预售商品id")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "预售价")
    private BigDecimal presalePrice;

    @Schema(description = "预售限购库存")
    private Integer stockLimit;

    @Schema(description = "已售数量")
    private Integer soldCount;

    @Schema(description = "剩余库存 = stockLimit - soldCount")
    private Integer remainStock;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "下单截止时间")
    private LocalDateTime endTime;

    @Schema(description = "统一发货日期")
    private LocalDate deliveryDate;

    @Schema(description = "状态 0未开始 1进行中 2已结束")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;
}
