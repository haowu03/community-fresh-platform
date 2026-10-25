package com.fresh.modules.cart.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车加购/减购入参
 */
@Data
@Schema(description = "购物车加购/减购入参")
public class ShoppingCartDTO implements Serializable {

    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @Schema(description = "份数(按份商品必传)")
    private Integer number;

    @Schema(description = "称重重量(按重量商品必传)")
    private BigDecimal weight;
}
