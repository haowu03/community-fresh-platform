package com.fresh.modules.order.vo;

import com.fresh.modules.order.entity.OrderDetail;
import com.fresh.modules.order.entity.Orders;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 订单展示对象：订单字段 + 明细列表 + 自提信息冗余
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单展示对象")
public class OrderVO extends Orders {

    /** 订单明细列表 */
    @Schema(description = "订单明细列表")
    private List<OrderDetail> orderDetailList;

    /** 自提点名称 */
    @Schema(description = "自提点名称")
    private String pickupPointName;

    /** 自提时间段描述，如 09:00~12:00 */
    @Schema(description = "自提时间段描述")
    private String timeSlotDesc;
}
