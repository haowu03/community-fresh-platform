package com.fresh.modules.groupbuy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发起团购入参
 */
@Data
@Schema(description = "发起团购入参")
public class GroupBuyLaunchDTO implements Serializable {

    @Schema(description = "团购商品id")
    private Long productId;

    @Schema(description = "统一配送自提点id")
    private Long pickupPointId;

    @Schema(description = "团购价")
    private BigDecimal groupPrice;

    @Schema(description = "成团人数")
    private Integer minNum;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "截止时间")
    private LocalDateTime endTime;
}
