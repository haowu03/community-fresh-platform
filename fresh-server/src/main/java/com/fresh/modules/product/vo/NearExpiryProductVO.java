package com.fresh.modules.product.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 临期特价商品 VO（商品 + 批次折扣价 + 到期日 + 剩余天数）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "临期特价商品VO")
public class NearExpiryProductVO extends ProductVO {

    @Schema(description = "临期批次id")
    private Long batchId;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "批次剩余库存")
    private Integer batchStock;

    @Schema(description = "折扣(如0.70=7折)")
    private BigDecimal discount;

    @Schema(description = "折后价 = 单价 × 折扣")
    private BigDecimal discountPrice;

    @Schema(description = "到期日期")
    private LocalDate expiryDate;

    @Schema(description = "剩余天数")
    private Long remainDays;
}
