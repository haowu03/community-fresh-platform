package com.fresh.modules.report.controller;

import com.fresh.common.result.R;
import com.fresh.modules.report.service.ReportService;
import com.fresh.modules.report.vo.LossReportVO;
import com.fresh.modules.report.vo.OrderReportVO;
import com.fresh.modules.report.vo.SalesTop10ReportVO;
import com.fresh.modules.report.vo.TurnoverReportVO;
import com.fresh.modules.report.vo.UserReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 管理端-数据报表接口
 */
@Slf4j
@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
@Tag(name = "报表模块", description = "管理端-数据统计报表接口")
public class ReportController {

    private final ReportService reportService;

    /**
     * 营业额统计（按日，区间内补零）
     */
    @GetMapping("/turnoverStatistics")
    @Operation(summary = "营业额统计")
    public R<TurnoverReportVO> turnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return R.success(reportService.turnoverStatistics(begin, end));
    }

    /**
     * 用户统计（每日总量/新增）
     */
    @GetMapping("/userStatistics")
    @Operation(summary = "用户统计")
    public R<UserReportVO> userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return R.success(reportService.userStatistics(begin, end));
    }

    /**
     * 订单统计（订单数/有效订单数/完成率）
     */
    @GetMapping("/orderStatistics")
    @Operation(summary = "订单统计")
    public R<OrderReportVO> orderStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return R.success(reportService.orderStatistics(begin, end));
    }

    /**
     * 销量 TOP10（仅已完成订单）
     */
    @GetMapping("/top10")
    @Operation(summary = "销量TOP10")
    public R<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        return R.success(reportService.top10(begin, end));
    }

    /**
     * 损耗统计（临期/过期批次金额与批次数）
     */
    @GetMapping("/lossStatistics")
    @Operation(summary = "损耗统计")
    public R<LossReportVO> lossStatistics() {
        return R.success(reportService.lossStatistics());
    }
}
