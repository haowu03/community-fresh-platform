package com.fresh.modules.pickup.controller;

import com.fresh.common.result.R;
import com.fresh.modules.pickup.entity.PickupTimeSlot;
import com.fresh.modules.pickup.service.PickupPointService;
import com.fresh.modules.pickup.vo.PickupPointVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端-自提点查询
 */
@Slf4j
@RestController
@RequestMapping("/user/pickup")
@RequiredArgsConstructor
@Tag(name = "用户端-自提点查询")
public class UserPickupController {

    private final PickupPointService pickupPointService;

    /**
     * 启用自提点列表
     */
    @GetMapping("/point/list")
    @Operation(summary = "启用自提点列表")
    public R<List<PickupPointVO>> list() {
        return R.success(pickupPointService.listEnabled());
    }

    /**
     * 自提点可用时间段（启用且未约满）
     */
    @GetMapping("/point/{id}/slots")
    @Operation(summary = "自提点可用时间段")
    public R<List<PickupTimeSlot>> slots(@PathVariable Long id) {
        return R.success(pickupPointService.listAvailableSlots(id));
    }
}
