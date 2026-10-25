package com.fresh.modules.pickup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * 自提时间段新增/修改入参
 */
@Data
@Schema(description = "自提时间段新增/修改入参")
public class TimeSlotDTO implements Serializable {

    @Schema(description = "主键(为空新增, 不为空修改)")
    private Long id;

    @Schema(description = "自提点id")
    private Long pointId;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "该时段最大订单数")
    private Integer maxOrders;

    @Schema(description = "状态 1启用 0停用")
    private Integer status;
}
