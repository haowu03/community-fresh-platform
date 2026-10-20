package com.fresh.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 自提码核销入参
 */
@Data
@Schema(description = "自提码核销入参")
public class OrderVerifyDTO implements Serializable {

    /** 自提码(6位数字) */
    @Schema(description = "自提码(6位数字)")
    private String pickupCode;
}
