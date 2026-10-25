package com.fresh.modules.report.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户统计返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户统计返回结果")
public class UserReportVO implements Serializable {

    /** 日期列表，逗号分隔 */
    @Schema(description = "日期列表，逗号分隔")
    private String dateList;

    /** 每日用户总量列表，逗号分隔 */
    @Schema(description = "每日用户总量列表，逗号分隔")
    private String totalUserList;

    /** 每日新增用户列表，逗号分隔 */
    @Schema(description = "每日新增用户列表，逗号分隔")
    private String newUserList;
}
