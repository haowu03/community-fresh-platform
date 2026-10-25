package com.fresh.modules.presale.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预售活动发布/修改入参
 */
@Data
@Schema(description = "预售活动发布/修改入参")
public class PresaleActivityDTO implements Serializable {

    @Schema(description = "主键(修改时传)")
    private Long id;

    @Schema(description = "预售商品id")
    private Long productId;

    @Schema(description = "预售价")
    private BigDecimal presalePrice;

    @Schema(description = "预售限购库存")
    private Integer stockLimit;

    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    @Schema(description = "下单截止时间")
    private LocalDateTime endTime;

    @Schema(description = "统一发货日期")
    private LocalDate deliveryDate;
}
