package com.fresh.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单支付入参
 */
@Data
@Schema(description = "订单支付入参")
public class OrdersPaymentDTO implements Serializable {

    /** 订单号 */
    @Schema(description = "订单号")
    private String orderNumber;

    /** 支付方式 1微信 2支付宝 */
    @Schema(description = "支付方式 1微信 2支付宝")
    private Integer payMethod;
}
