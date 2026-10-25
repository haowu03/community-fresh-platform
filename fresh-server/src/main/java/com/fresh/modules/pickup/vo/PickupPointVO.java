package com.fresh.modules.pickup.vo;

import com.fresh.modules.pickup.entity.PickupTimeSlot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 自提点展示VO（含团长昵称与时间段）
 */
@Data
@Schema(description = "自提点展示VO")
public class PickupPointVO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "自提点名称")
    private String name;

    @Schema(description = "绑定团长user.id")
    private Long leaderId;

    @Schema(description = "团长昵称")
    private String leaderName;

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

    @Schema(description = "自提时间段列表")
    private List<PickupTimeSlot> timeSlots;
}
