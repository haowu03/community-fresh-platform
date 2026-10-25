package com.fresh.modules.report.service.impl;

import cn.hutool.core.util.StrUtil;
import com.fresh.modules.report.mapper.ReportMapper;
import com.fresh.modules.report.service.ReportService;
import com.fresh.modules.report.vo.LossReportVO;
import com.fresh.modules.report.vo.OrderReportVO;
import com.fresh.modules.report.vo.SalesTop10ReportVO;
import com.fresh.modules.report.vo.TurnoverReportVO;
import com.fresh.modules.report.vo.UserReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报表统计 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    /** 批次状态：正常 */
    private static final Integer BATCH_STATUS_NORMAL = 1;
    /** 批次状态：临期 */
    private static final Integer BATCH_STATUS_NEAR_EXPIRY = 2;
    /** 批次状态：过期 */
    private static final Integer BATCH_STATUS_EXPIRED = 3;

    private final ReportMapper reportMapper;

    /**
     * 营业额统计：逐日查询已完成订单营业额，区间内无数据日期补零
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        // 生成日期区间列表（闭区间，逐日递增）
        List<LocalDate> dateList = buildDateList(begin, end);

        // 逐日统计营业额，null 补零
        List<BigDecimal> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            BigDecimal turnover = reportMapper.sumTurnoverByDate(date);
            turnoverList.add(turnover == null ? BigDecimal.ZERO : turnover);
        }

        return TurnoverReportVO.builder()
                .dateList(StrUtil.join(",", dateList))
                .turnoverList(StrUtil.join(",", turnoverList))
                .build();
    }

    /**
     * 用户统计：逐日统计用户总量与新增用户数
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = buildDateList(begin, end);

        List<Integer> totalUserList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            Integer total = reportMapper.countTotalUserByDate(date);
            Integer newUser = reportMapper.countNewUserByDate(date);
            totalUserList.add(total == null ? 0 : total);
            newUserList.add(newUser == null ? 0 : newUser);
        }

        return UserReportVO.builder()
                .dateList(StrUtil.join(",", dateList))
                .totalUserList(StrUtil.join(",", totalUserList))
                .newUserList(StrUtil.join(",", newUserList))
                .build();
    }

    /**
     * 订单统计：逐日统计订单数与有效订单数，并汇总计算完成率
     */
    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = buildDateList(begin, end);

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        int totalOrderCount = 0;
        int validOrderCount = 0;
        for (LocalDate date : dateList) {
            Integer orderCount = reportMapper.countOrdersByDate(date);
            Integer validCount = reportMapper.countValidOrdersByDate(date);
            orderCount = orderCount == null ? 0 : orderCount;
            validCount = validCount == null ? 0 : validCount;
            orderCountList.add(orderCount);
            validOrderCountList.add(validCount);
            totalOrderCount += orderCount;
            validOrderCount += validCount;
        }

        // 订单完成率 = 有效订单数 / 订单总数（分母为 0 时按 0 处理）
        Double completionRate = totalOrderCount == 0 ? 0.0 : validOrderCount * 1.0 / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StrUtil.join(",", dateList))
                .orderCountList(StrUtil.join(",", orderCountList))
                .validOrderCountList(StrUtil.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(completionRate)
                .build();
    }

    /**
     * 销量 TOP10：区间内已完成订单按商品分组销量前 10
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        List<Map<String, Object>> list = reportMapper.top10(begin, end);

        List<String> nameList = new ArrayList<>();
        List<Object> numberList = new ArrayList<>();
        for (Map<String, Object> map : list) {
            nameList.add(String.valueOf(map.get("name")));
            numberList.add(map.get("number"));
        }

        return SalesTop10ReportVO.builder()
                .nameList(StrUtil.join(",", nameList))
                .numberList(StrUtil.join(",", numberList))
                .build();
    }

    /**
     * 损耗统计：按批次状态汇总库存金额与批次数
     */
    @Override
    public LossReportVO lossStatistics() {
        List<Map<String, Object>> list = reportMapper.sumBatchLossByStatus();

        LossReportVO vo = LossReportVO.builder()
                .normalAmount(BigDecimal.ZERO)
                .nearExpiryAmount(BigDecimal.ZERO)
                .expiredAmount(BigDecimal.ZERO)
                .nearExpiryCount(0)
                .expiredCount(0)
                .build();

        // 按批次状态分拣汇总结果
        for (Map<String, Object> map : list) {
            Integer status = Integer.valueOf(String.valueOf(map.get("status")));
            BigDecimal amount = new BigDecimal(String.valueOf(map.get("amount")));
            Integer batchCount = Integer.valueOf(String.valueOf(map.get("batchCount")));
            if (BATCH_STATUS_NORMAL.equals(status)) {
                vo.setNormalAmount(amount);
            } else if (BATCH_STATUS_NEAR_EXPIRY.equals(status)) {
                vo.setNearExpiryAmount(amount);
                vo.setNearExpiryCount(batchCount);
            } else if (BATCH_STATUS_EXPIRED.equals(status)) {
                vo.setExpiredAmount(amount);
                vo.setExpiredCount(batchCount);
            }
        }
        return vo;
    }

    /**
     * 构建日期区间列表（闭区间，逐日递增）
     */
    private List<LocalDate> buildDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date);
        }
        return dateList;
    }
}
