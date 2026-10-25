package com.fresh.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单拒单入参（管理端）
 */
@Data
@Schema(description = "订单拒单入参")
public class OrdersRejectionDTO implements Serializable {

    /** 订单id */
    @Schema(description = "订单id")
    private Long id;

    /** 拒单原因 */
    @Schema(description = "拒单原因")
    private String rejectionReason;
}
