package com.fresh.modules.order.controller;

import com.fresh.common.context.BaseContext;
import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.order.dto.OrderVerifyDTO;
import com.fresh.modules.order.dto.OrdersCancelDTO;
import com.fresh.modules.order.dto.OrdersPageQueryDTO;
import com.fresh.modules.order.dto.OrdersRejectionDTO;
import com.fresh.modules.order.service.OrderService;
import com.fresh.modules.order.vo.OrderStatisticsVO;
import com.fresh.modules.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-订单接口（搜索/统计/接单/拒单/派送/完成/取消/核销）
 */
@Slf4j
@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
@Tag(name = "管理端-订单接口")
public class AdminOrderController {

    private final OrderService orderService;

    /**
     * 订单条件分页搜索
     */
    @GetMapping("/conditionSearch")
    @Operation(summary = "订单条件搜索")
    public R<PageResult> conditionSearch(OrdersPageQueryDTO dto) {
        return R.success(orderService.conditionSearch(dto));
    }

    /**
     * 订单详情
     */
    @GetMapping("/details/{id}")
    @Operation(summary = "查询订单详情")
    public R<OrderVO> details(@PathVariable Long id) {
        return R.success(orderService.details(id));
    }

    /**
     * 各状态订单数量统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "各状态订单数量统计")
    public R<OrderStatisticsVO> statistics() {
        return R.success(orderService.statistics());
    }

    /**
     * 接单
     */
    @PutMapping("/confirm")
    @Operation(summary = "接单")
    public R<String> confirm(@RequestParam Long id) {
        orderService.confirm(id);
        return R.success();
    }

    /**
     * 拒单
     */
    @PutMapping("/rejection")
    @Operation(summary = "拒单")
    public R<String> rejection(@RequestBody OrdersRejectionDTO dto) {
        orderService.rejection(dto);
        return R.success();
    }

    /**
     * 派送 / 备货完成（自提单此时生成自提码并通知）
     */
    @PutMapping("/delivery/{id}")
    @Operation(summary = "派送/备货完成")
    public R<String> delivery(@PathVariable Long id) {
        orderService.delivery(id);
        return R.success();
    }

    /**
     * 完成订单
     */
    @PutMapping("/complete/{id}")
    @Operation(summary = "完成订单")
    public R<String> complete(@PathVariable Long id) {
        orderService.complete(id);
        return R.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    @Operation(summary = "取消订单")
    public R<String> cancel(@RequestBody OrdersCancelDTO dto) {
        orderService.adminCancel(dto);
        return R.success();
    }

    /**
     * 自提码核销（管理员）
     */
    @PutMapping("/verify")
    @Operation(summary = "自提码核销")
    public R<String> verify(@RequestBody OrderVerifyDTO dto) {
        orderService.verify(dto, BaseContext.getCurrentId(), true);
        return R.success();
    }
}
