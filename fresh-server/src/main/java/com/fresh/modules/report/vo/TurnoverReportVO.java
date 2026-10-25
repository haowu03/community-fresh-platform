package com.fresh.modules.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 营业额统计返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "营业额统计返回结果")
public class TurnoverReportVO implements Serializable {

    /** 日期列表，逗号分隔，如 2026-10-01,2026-10-02 */
    @Schema(description = "日期列表，逗号分隔")
    private String dateList;

    /** 营业额列表，逗号分隔，与日期一一对应 */
    @Schema(description = "营业额列表，逗号分隔")
    private String turnoverList;
}
