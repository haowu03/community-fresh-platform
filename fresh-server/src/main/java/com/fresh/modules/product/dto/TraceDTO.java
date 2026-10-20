package com.fresh.modules.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 溯源信息维护入参
 */
@Data
@Schema(description = "溯源信息维护入参")
public class TraceDTO implements Serializable {

    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @Schema(description = "产地")
    private String originPlace;

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
