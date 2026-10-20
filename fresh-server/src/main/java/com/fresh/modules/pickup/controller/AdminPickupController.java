package com.fresh.modules.pickup.controller;

import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.pickup.dto.PickupPointDTO;
import com.fresh.modules.pickup.dto.PickupPointPageQueryDTO;
import com.fresh.modules.pickup.dto.TimeSlotDTO;
import com.fresh.modules.pickup.entity.PickupTimeSlot;
import com.fresh.modules.pickup.service.PickupPointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-自提点管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/pickup/point")
@RequiredArgsConstructor
@Tag(name = "管理端-自提点管理")
public class AdminPickupController {

    private final PickupPointService pickupPointService;

    /**
     * 自提点分页查询
     */
    @GetMapping("/page")
    @Operation(summary = "自提点分页查询")
    public R<PageResult> page(PickupPointPageQueryDTO dto) {
        log.info("自提点分页查询: {}", dto);
        return R.success(pickupPointService.pageQuery(dto));
    }

    /**
     * 新增自提点
     */
    @PostMapping
    @Operation(summary = "新增自提点")
    public R<Void> add(@RequestBody PickupPointDTO dto) {
        log.info("新增自提点: {}", dto);
        pickupPointService.add(dto);
        return R.success();
    }

    /**
     * 修改自提点
     */
    @PutMapping
    @Operation(summary = "修改自提点")
    public R<Void> update(@RequestBody PickupPointDTO dto) {
        log.info("修改自提点: {}", dto);
        pickupPointService.update(dto);
        return R.success();
    }

    /**
     * 删除自提点（有进行中订单的自提点禁止删除）
     */
    @DeleteMapping
    @Operation(summary = "删除自提点")
    public R<Void> delete(@RequestParam Long id) {
        log.info("删除自提点: {}", id);
        pickupPointService.deleteById(id);
        return R.success();
    }

    /**
     * 启用/停用自提点
     */
    @PutMapping("/status/{status}")
    @Operation(summary = "启用/停用自提点")
    public R<Void> updateStatus(@PathVariable Integer status, @RequestParam Long id) {
        log.info("自提点启停: id={}, status={}", id, status);
        pickupPointService.updateStatus(id, status);
        return R.success();
    }

    /**
     * 新增/修改自提时间段（id为空新增）
     */
    @PostMapping("/slot")
    @Operation(summary = "新增/修改自提时间段")
    public R<Void> saveOrUpdateSlot(@RequestBody TimeSlotDTO dto) {
        log.info("保存自提时间段: {}", dto);
        pickupPointService.saveOrUpdateSlot(dto);
        return R.success();
    }

    /**
     * 删除自提时间段
     */
    @DeleteMapping("/slot/{id}")
    @Operation(summary = "删除自提时间段")
    public R<Void> deleteSlot(@PathVariable Long id) {
        log.info("删除自提时间段: {}", id);
        pickupPointService.deleteSlot(id);
        return R.success();
    }

    /**
     * 查询自提点时间段列表
     */
    @GetMapping("/slot/list")
    @Operation(summary = "查询自提点时间段列表")
    public R<List<PickupTimeSlot>> listSlots(@RequestParam Long pointId) {
        return R.success(pickupPointService.listSlots(pointId));
    }
}
