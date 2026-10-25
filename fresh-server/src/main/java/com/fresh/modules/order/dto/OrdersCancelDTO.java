package com.fresh.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单取消入参（管理端）
 */
@Data
@Schema(description = "订单取消入参")
public class OrdersCancelDTO implements Serializable {

    /** 订单id */
    @Schema(description = "订单id")
    private Long id;

    /** 取消原因 */
    @Schema(description = "取消原因")
    private String cancelReason;
}
