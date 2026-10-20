package com.fresh.modules.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品分页查询入参
 */
@Data
@Schema(description = "商品分页查询入参")
public class ProductPageQueryDTO implements Serializable {

    @Schema(description = "页码", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer page;

    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pageSize;

    @Schema(description = "商品名称(模糊)")
    private String name;

    @Schema(description = "分类id")
    private Long categoryId;

    @Schema(description = "状态 1起售 0停售")
    private Integer status;

    @Schema(description = "保鲜类型 0常温 1冷藏 2冷冻")
    private Integer freshType;
}
