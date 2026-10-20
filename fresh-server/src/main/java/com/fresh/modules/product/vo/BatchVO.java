package com.fresh.modules.product.vo;

import com.fresh.modules.product.entity.ProductBatch;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商品批次 VO
 */
@Data
@Schema(description = "商品批次VO")
public class BatchVO {

    @Schema(description = "批次id")
    private Long id;

    @Schema(description = "商品id")
    private Long productId;

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

    @Schema(description = "折扣(如0.70=7折)")
    private BigDecimal discount;

    @Schema(description = "状态 1正常 2临期 3过期")
    private Integer status;

    @Schema(description = "状态中文描述")
    private String statusDesc;

    public static BatchVO of(ProductBatch batch) {
        BatchVO vo = new BatchVO();
        vo.setId(batch.getId());
        vo.setProductId(batch.getProductId());
        vo.setBatchNo(batch.getBatchNo());
        vo.setProductionDate(batch.getProductionDate());
        vo.setExpiryDate(batch.getExpiryDate());
        vo.setStock(batch.getStock());
        vo.setInitStock(batch.getInitStock());
        vo.setDiscount(batch.getDiscount());
        vo.setStatus(batch.getStatus());
        // 批次状态中文描述
        vo.setStatusDesc(switch (batch.getStatus() == null ? -1 : batch.getStatus()) {
            case 1 -> "正常";
            case 2 -> "临期";
            case 3 -> "过期";
            default -> "未知";
        });
        return vo;
    }
}
