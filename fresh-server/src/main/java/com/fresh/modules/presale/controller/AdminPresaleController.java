package com.fresh.modules.presale.controller;

import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.presale.dto.PresaleActivityDTO;
import com.fresh.modules.presale.dto.PresalePageQueryDTO;
import com.fresh.modules.presale.service.PresaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-预售活动管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/presale")
@RequiredArgsConstructor
@Tag(name = "管理端-预售活动管理")
public class AdminPresaleController {

    private final PresaleService presaleService;

    /**
     * 预售活动分页查询
     */
    @GetMapping("/page")
    @Operation(summary = "预售活动分页查询")
    public R<PageResult> page(PresalePageQueryDTO dto) {
        log.info("预售活动分页查询: {}", dto);
        return R.success(presaleService.pageQuery(dto));
    }

    /**
     * 发布预售活动
     */
    @PostMapping
    @Operation(summary = "发布预售活动")
    public R<Void> publish(@RequestBody PresaleActivityDTO dto) {
        log.info("发布预售活动: {}", dto);
        presaleService.publish(dto);
        return R.success();
    }

    /**
     * 修改预售活动
     */
    @PutMapping
    @Operation(summary = "修改预售活动")
    public R<Void> update(@RequestBody PresaleActivityDTO dto) {
        log.info("修改预售活动: {}", dto);
        presaleService.update(dto);
        return R.success();
    }

    /**
     * 手动提前结束
     */
    @PutMapping("/close/{id}")
    @Operation(summary = "手动提前结束预售活动")
    public R<Void> close(@PathVariable Long id) {
        log.info("手动结束预售活动: {}", id);
        presaleService.close(id);
        return R.success();
    }

    /**
     * 删除预售活动
     */
    @DeleteMapping
    @Operation(summary = "删除预售活动")
    public R<Void> delete(@RequestParam Long id) {
        log.info("删除预售活动: {}", id);
        presaleService.deleteById(id);
        return R.success();
    }
}
