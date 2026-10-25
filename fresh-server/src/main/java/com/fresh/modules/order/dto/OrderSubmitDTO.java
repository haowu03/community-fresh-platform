package com.fresh.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提交订单入参（普通/自提/预售/团购通用入口）
 */
@Data
@Schema(description = "提交订单入参")
public class OrderSubmitDTO implements Serializable {

    /** 地址id(配送单必填) */
    @Schema(description = "地址id(配送单必填)")
    private Long addressBookId;

    /** 自提点id(自提单必填，团购单自动取活动自提点) */
    @Schema(description = "自提点id(自提单必填)")
    private Long pickupPointId;

    /** 自提时间段id(自提单必填) */
    @Schema(description = "自提时间段id(自提单必填)")
    private Long timeSlotId;

    /** 履约方式 1配送到家 2自提 */
    @Schema(description = "履约方式 1配送到家 2自提")
    private Integer deliveryType;

    /** 订单类型 1普通 2预售 3团购，默认1 */
    @Schema(description = "订单类型 1普通 2预售 3团购，默认1")
    private Integer orderType = 1;

    /** 预售活动id(预售单必填) */
    @Schema(description = "预售活动id(预售单必填)")
    private Long presaleId;

    /** 团购活动id(团购单必填) */
    @Schema(description = "团购活动id(团购单必填)")
    private Long groupActivityId;

    /** 商品id(预售/团购单必填) */
    @Schema(description = "商品id(预售/团购单必填)")
    private Long productId;

    /** 份数(预售/团购单) */
    @Schema(description = "份数")
    private Integer number;

    /** 称重重量(按重量商品) */
    @Schema(description = "称重重量(按重量商品)")
    private BigDecimal weight;

    /** 备注 */
    @Schema(description = "备注")
    private String remark;

    /** 支付方式 1微信 2支付宝 */
    @Schema(description = "支付方式 1微信 2支付宝")
    private Integer payMethod;

    /** 打包费 */
    @Schema(description = "打包费")
    private BigDecimal packAmount;
}
