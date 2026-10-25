package com.fresh.modules.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商品新增/修改入参（含生鲜特色字段，可顺带维护溯源）
 */
@Data
@Schema(description = "商品新增/修改入参")
public class ProductDTO implements Serializable {

    @Schema(description = "主键(修改时必传)")
    private Long id;

    @Schema(description = "商品名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "分类id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long categoryId;

    @Schema(description = "单价(每份/每斤/每kg)", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "计价方式 1按份 2按重量")
    private Integer priceType;

    @Schema(description = "计价单位: 份/斤/kg")
    private String unit;

    @Schema(description = "保鲜类型 0常温 1冷藏 2冷冻")
    private Integer freshType;

    @Schema(description = "保质期(天)")
    private Integer shelfLifeDays;

    @Schema(description = "总库存")
    private Integer stock;

    @Schema(description = "预警库存")
    private Integer warnStock;

    @Schema(description = "产地")
    private String originPlace;

    @Schema(description = "溯源码")
    private String traceCode;

    @Schema(description = "商品图片")
    private String image;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "是否预售 0否 1是")
    private Integer isPresale;

    // ================= 溯源信息（可选，填写后顺带 upsert） =================

    @Schema(description = "种植户/养殖户")
    private String farmer;

    @Schema(description = "种植日期")
    private LocalDate plantDate;

    @Schema(description = "采摘/出栏日期")
    private LocalDate pickDate;

    @Schema(description = "检测报告(图片url)")
    private String testReport;

    @Schema(description = "运输/冷链信息")
    private String transportInfo;
}
