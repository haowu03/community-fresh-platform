package com.fresh.modules.product.vo;

import com.fresh.modules.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品 VO（含分类名、保鲜类型中文描述、最新批次）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品VO")
public class ProductVO extends Product {

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "保鲜类型中文描述")
    private String freshTypeDesc;

    @Schema(description = "最新批次信息")
    private BatchVO latestBatch;
}
