package com.fresh.modules.order.task;

import com.fresh.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 超时未支付订单定时取消任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderService orderService;

    /**
     * 每分钟扫描一次：超时15分钟未支付的订单自动取消并回滚库存/时间段
     */
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrders() {
        try {
            orderService.cancelTimeoutOrders();
        } catch (Exception e) {
            log.error("超时订单自动取消任务执行异常", e);
        }
    }
}
