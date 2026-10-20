package com.fresh.modules.groupbuy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 参团成员展示VO
 */
@Data
@Schema(description = "参团成员展示VO")
public class GroupBuyMemberVO implements Serializable {

    @Schema(description = "参团记录id")
    private Long id;

    @Schema(description = "团购活动id")
    private Long activityId;

    @Schema(description = "参团用户id")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "关联订单id")
    private Long orderId;

    @Schema(description = "订单金额")
    private BigDecimal orderAmount;

    @Schema(description = "状态 1已支付 2已退款")
    private Integer status;

    @Schema(description = "参团时间")
    private LocalDateTime createTime;
}
