package com.fresh.modules.loss.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商品批次展示VO（含商品名称，用于管理端临期/过期批次查看）
 */
@Data
@Schema(description = "商品批次展示VO")
public class ProductBatchVO implements Serializable {

    @Schema(description = "批次id")
    private Long id;

    @Schema(description = "商品id")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "生产日期")
    private LocalDate productionDate;

    @Schema(description = "到期日期")
    private LocalDate expiryDate;

    @Schema(description = "批次剩余库存")
    private Integer stock;

    @Schema(description = "批次初始库存")
    private Integer initStock;

    @Schema(description = "折扣")
    private BigDecimal discount;

    @Schema(description = "状态 1正常 2临期 3过期")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;
}
