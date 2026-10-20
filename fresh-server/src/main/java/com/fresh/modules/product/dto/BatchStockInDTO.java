package com.fresh.modules.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 批次入库入参
 * 到期日 = 生产日期 + 商品保质期，由服务端计算
 */
@Data
@Schema(description = "批次入库入参")
public class BatchStockInDTO implements Serializable {

    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @Schema(description = "生产日期", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate productionDate;

    @Schema(description = "入库数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer stock;
}
