package com.fresh.modules.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 销量 TOP10 返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "销量TOP10返回结果")
public class SalesTop10ReportVO implements Serializable {

    /** 商品名称列表，逗号分隔 */
    @Schema(description = "商品名称列表，逗号分隔")
    private String nameList;

    /** 销量列表，逗号分隔，与名称一一对应 */
    @Schema(description = "销量列表，逗号分隔")
    private String numberList;
}
