package com.fresh.modules.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单统计返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单统计返回结果")
public class OrderReportVO implements Serializable {

    /** 日期列表，逗号分隔 */
    @Schema(description = "日期列表，逗号分隔")
    private String dateList;

    /** 每日订单数列表，逗号分隔 */
    @Schema(description = "每日订单数列表，逗号分隔")
    private String orderCountList;

    /** 每日有效订单数（已完成）列表，逗号分隔 */
    @Schema(description = "每日有效订单数列表，逗号分隔")
    private String validOrderCountList;

    /** 区间内订单总数 */
    @Schema(description = "订单总数")
    private Integer totalOrderCount;

    /** 区间内有效订单总数 */
    @Schema(description = "有效订单总数")
    private Integer validOrderCount;

    /** 订单完成率（有效订单/订单总数） */
    @Schema(description = "订单完成率")
    private Double orderCompletionRate;
}
