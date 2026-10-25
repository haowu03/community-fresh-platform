package com.fresh.modules.order.controller;

import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.evaluation.service.EvaluationService;
import com.fresh.modules.evaluation.vo.EvaluationVO;
import com.fresh.modules.order.dto.EvaluationDTO;
import com.fresh.modules.order.dto.OrderSubmitDTO;
import com.fresh.modules.order.dto.OrdersPageQueryDTO;
import com.fresh.modules.order.dto.OrdersPaymentDTO;
import com.fresh.modules.order.service.OrderService;
import com.fresh.modules.order.vo.OrderSubmitVO;
import com.fresh.modules.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端-订单接口（下单/支付/历史订单/取消/再来一单/催单/评价）
 */
@Slf4j
@RestController
@RequestMapping("/user/order")
@RequiredArgsConstructor
@Tag(name = "用户端-订单接口")
public class OrderController {

    private final OrderService orderService;
    private final EvaluationService evaluationService;

    /**
     * 提交订单（普通/自提/预售/团购通用入口）
     */
    @PostMapping("/submit")
    @Operation(summary = "提交订单")
    public R<OrderSubmitVO> submit(@RequestBody OrderSubmitDTO dto) {
        log.info("用户提交订单：{}", dto);
        return R.success(orderService.submit(dto));
    }

    /**
     * 模拟支付
     */
    @PutMapping("/payment")
    @Operation(summary = "订单支付")
    public R<String> payment(@RequestBody OrdersPaymentDTO dto) {
        log.info("订单支付：{}", dto);
        orderService.payment(dto);
        return R.success();
    }

    /**
     * 历史订单分页查询
     */
    @GetMapping("/historyOrders")
    @Operation(summary = "历史订单分页查询")
    public R<PageResult> historyOrders(OrdersPageQueryDTO dto) {
        return R.success(orderService.historyOrders(dto));
    }

    /**
     * 订单详情
     */
    @GetMapping("/orderDetail/{id}")
    @Operation(summary = "查询订单详情")
    public R<OrderVO> orderDetail(@PathVariable Long id) {
        return R.success(orderService.orderDetail(id));
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    @Operation(summary = "取消订单")
    public R<String> cancel(@PathVariable Long id) {
        orderService.userCancel(id);
        return R.success();
    }

    /**
     * 再来一单
     */
    @PostMapping("/repetition/{id}")
    @Operation(summary = "再来一单")
    public R<String> repetition(@PathVariable Long id) {
        orderService.repetition(id);
        return R.success();
    }

    /**
     * 催单
     */
    @GetMapping("/reminder/{id}")
    @Operation(summary = "客户催单")
    public R<String> reminder(@PathVariable Long id) {
        orderService.reminder(id);
        return R.success();
    }

    /**
     * 提交商品评价
     */
    @PostMapping("/evaluation")
    @Operation(summary = "提交商品评价")
    public R<String> evaluation(@RequestBody EvaluationDTO dto) {
        evaluationService.submit(dto);
        return R.success();
    }

    /**
     * 商品评价列表
     */
    @GetMapping("/evaluation/list")
    @Operation(summary = "商品评价列表")
    public R<List<EvaluationVO>> evaluationList(@RequestParam Long productId) {
        return R.success(evaluationService.listByProduct(productId));
    }
}
