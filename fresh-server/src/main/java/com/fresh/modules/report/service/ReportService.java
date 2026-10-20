package com.fresh.modules.report.service;

import com.fresh.modules.report.vo.LossReportVO;
import com.fresh.modules.report.vo.OrderReportVO;
import com.fresh.modules.report.vo.SalesTop10ReportVO;
import com.fresh.modules.report.vo.TurnoverReportVO;
import com.fresh.modules.report.vo.UserReportVO;

import java.time.LocalDate;

/**
 * 报表统计 Service
 */
public interface ReportService {

    /**
     * 营业额统计：区间内每日已完成订单营业额（无数据日期补零）
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 营业额统计
     */
    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计：区间内每日用户总量与新增用户数
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 用户统计
     */
    UserReportVO userStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计：区间内每日订单数、有效订单数、完成率
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 订单统计
     */
    OrderReportVO orderStatistics(LocalDate begin, LocalDate end);

    /**
     * 销量 TOP10：区间内已完成订单按商品分组销量前 10
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 销量 TOP10
     */
    SalesTop10ReportVO top10(LocalDate begin, LocalDate end);

    /**
     * 损耗统计：临期/过期批次库存金额与批次数
     *
     * @return 损耗统计
     */
    LossReportVO lossStatistics();
}
