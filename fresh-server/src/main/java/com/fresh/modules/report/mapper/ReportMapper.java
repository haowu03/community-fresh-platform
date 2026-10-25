package com.fresh.modules.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 报表统计 Mapper：营业额/用户/订单/销量TOP10/批次损耗
 * 复杂统计 SQL 统一写在 ReportMapper.xml
 */
@Mapper
public interface ReportMapper {

    /**
     * 统计某日已完成订单的营业额
     *
     * @param date 日期
     * @return 营业额，无订单返回 null
     */
    BigDecimal sumTurnoverByDate(@Param("date") LocalDate date);

    /**
     * 统计某日新增用户数
     *
     * @param date 日期
     * @return 新增用户数
     */
    Integer countNewUserByDate(@Param("date") LocalDate date);

    /**
     * 统计截至某日的用户总量
     *
     * @param date 日期
     * @return 用户总量
     */
    Integer countTotalUserByDate(@Param("date") LocalDate date);

    /**
     * 统计某日订单总数
     *
     * @param date 日期
     * @return 订单数
     */
    Integer countOrdersByDate(@Param("date") LocalDate date);

    /**
     * 统计某日有效订单数（已完成）
     *
     * @param date 日期
     * @return 有效订单数
     */
    Integer countValidOrdersByDate(@Param("date") LocalDate date);

    /**
     * 统计区间内销量 TOP10 商品（仅已完成订单）
     *
     * @param begin 开始日期
     * @param end   结束日期
     * @return 商品名称/销量 键值对列表
     */
    List<Map<String, Object>> top10(@Param("begin") LocalDate begin, @Param("end") LocalDate end);

    /**
     * 按批次状态汇总库存金额与批次数（join product 取单价）
     *
     * @return 状态/金额/批次数 列表
     */
    List<Map<String, Object>> sumBatchLossByStatus();
}
