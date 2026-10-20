package com.fresh.modules.pickup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 自提点新增/修改入参
 */
@Data
@Schema(description = "自提点新增/修改入参")
public class PickupPointDTO implements Serializable {

    @Schema(description = "主键(修改时传)")
    private Long id;

    @Schema(description = "自提点名称")
    private String name;

    @Schema(description = "绑定团长user.id")
    private Long leaderId;

    @Schema(description = "省")
    private String provinceName;

    @Schema(description = "市")
    private String cityName;

    @Schema(description = "区")
    private String districtName;

    @Schema(description = "详细地址")
    private String detailAddress;

    @Schema(description = "经度")
    private BigDecimal lng;

    @Schema(description = "纬度")
    private BigDecimal lat;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "营业时间描述")
    private String businessHours;

    @Schema(description = "状态 1启用 0停用")
    private Integer status;
}
