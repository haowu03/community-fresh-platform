package com.fresh.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.common.result.PageResult;
import com.fresh.modules.order.dto.*;
import com.fresh.modules.order.entity.Orders;
import com.fresh.modules.order.vo.OrderStatisticsVO;
import com.fresh.modules.order.vo.OrderSubmitVO;
import com.fresh.modules.order.vo.OrderVO;

/**
 * 订单服务
 */
public interface OrderService extends IService<Orders> {

    /**
     * 提交订单（普通/自提/预售/团购通用入口）
     */
    OrderSubmitVO submit(OrderSubmitDTO dto);

    /**
     * 模拟支付
     */
    void payment(OrdersPaymentDTO dto);

    /**
     * 用户端历史订单分页（带明细）
     */
    PageResult historyOrders(OrdersPageQueryDTO dto);

    /**
     * 用户端订单详情
     */
    OrderVO orderDetail(Long id);

    /**
     * 用户端取消订单
     */
    void userCancel(Long id);

    /**
     * 再来一单：将订单明细重新加入购物车
     */
    void repetition(Long id);

    /**
     * 催单（打印日志模拟）
     */
    void reminder(Long id);

    /**
     * 管理端条件分页搜索
     */
    PageResult conditionSearch(OrdersPageQueryDTO dto);

    /**
     * 管理端订单详情
     */
    OrderVO details(Long id);

    /**
     * 管理端各状态订单数量统计
     */
    OrderStatisticsVO statistics();

    /**
     * 接单：待接单 → 备货中
     */
    void confirm(Long id);

    /**
     * 拒单：已支付订单退款并回滚库存
     */
    void rejection(OrdersRejectionDTO dto);

    /**
     * 派送/备货完成：自提单生成自提码并通知，配送单进入配送中
     */
    void delivery(Long id);

    /**
     * 完成订单
     */
    void complete(Long id);

    /**
     * 管理端取消订单
     */
    void adminCancel(OrdersCancelDTO dto);

    /**
     * 自提码核销
     *
     * @param dto            核销入参（自提码）
     * @param operatorUserId 操作人id（团长为 user.id，管理员为 employee.id）
     * @param isAdmin        是否管理员；非管理员（团长）时校验订单自提点归属
     */
    void verify(OrderVerifyDTO dto, Long operatorUserId, boolean isAdmin);

    /**
     * 超时未支付订单自动取消（定时任务调用）
     */
    void cancelTimeoutOrders();
}
