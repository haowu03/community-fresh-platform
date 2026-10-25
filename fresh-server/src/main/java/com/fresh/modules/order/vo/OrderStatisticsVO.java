package com.fresh.modules.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单各状态数量统计（管理端）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单各状态数量统计")
public class OrderStatisticsVO implements Serializable {

    /** 待接单数量 */
    @Schema(description = "待接单数量")
    private Integer toBeConfirmed;

    /** 备货中/待配送数量 */
    @Schema(description = "备货中/待配送数量")
    private Integer confirmed;

    /** 配送中/待自提数量 */
    @Schema(description = "配送中/待自提数量")
    private Integer deliveryInProgress;
}
