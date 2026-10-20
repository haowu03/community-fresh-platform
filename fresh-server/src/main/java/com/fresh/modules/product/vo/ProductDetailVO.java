package com.fresh.modules.product.vo;

import com.fresh.modules.product.entity.ProductTrace;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品详情 VO（商品 + 溯源 + 批次列表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品详情VO")
public class ProductDetailVO extends ProductVO {

    @Schema(description = "溯源信息")
    private ProductTrace trace;

    @Schema(description = "批次列表")
    private List<BatchVO> batches;
}
