package com.fresh.modules.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 损耗统计返回结果（批次临期/过期）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "损耗统计返回结果")
public class LossReportVO implements Serializable {

    /** 正常批次库存货值金额 */
    @Schema(description = "正常批次库存货值金额")
    private BigDecimal normalAmount;

    /** 临期批次库存金额 */
    @Schema(description = "临期批次库存金额")
    private BigDecimal nearExpiryAmount;

    /** 过期批次损耗金额 */
    @Schema(description = "过期批次损耗金额")
    private BigDecimal expiredAmount;

    /** 临期批次数 */
    @Schema(description = "临期批次数")
    private Integer nearExpiryCount;

    /** 过期批次数 */
    @Schema(description = "过期批次数")
    private Integer expiredCount;
}
