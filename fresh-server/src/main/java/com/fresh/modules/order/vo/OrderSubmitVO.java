package com.fresh.modules.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提交订单返回对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提交订单返回对象")
public class OrderSubmitVO implements Serializable {

    /** 订单id */
    @Schema(description = "订单id")
    private Long id;

    /** 订单号 */
    @Schema(description = "订单号")
    private String orderNumber;

    /** 订单金额 */
    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    /** 下单时间 */
    @Schema(description = "下单时间")
    private LocalDateTime orderTime;
}
