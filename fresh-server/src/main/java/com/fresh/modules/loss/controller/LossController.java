package com.fresh.modules.loss.controller;

import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.loss.service.LossService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端-损耗/临期批次管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/loss")
@RequiredArgsConstructor
@Tag(name = "管理端-损耗/临期批次管理")
public class LossController {

    private final LossService lossService;

    /**
     * 批次分页查询（查看临期/过期批次）
     */
    @GetMapping("/batch/page")
    @Operation(summary = "批次分页查询")
    public R<PageResult> batchPage(@RequestParam(required = false) Integer status,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("批次分页查询: status={}, page={}, pageSize={}", status, page, pageSize);
        return R.success(lossService.batchPage(status, page, pageSize));
    }
}
