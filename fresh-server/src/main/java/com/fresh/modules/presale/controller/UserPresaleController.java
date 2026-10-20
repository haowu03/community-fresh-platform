package com.fresh.modules.presale.controller;

import com.fresh.common.result.R;
import com.fresh.modules.presale.service.PresaleService;
import com.fresh.modules.presale.vo.PresaleActivityVO;
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
 * 用户端-预售活动
 */
@Slf4j
@RestController
@RequestMapping("/user/presale")
@RequiredArgsConstructor
@Tag(name = "用户端-预售活动")
public class UserPresaleController {

    private final PresaleService presaleService;

    /**
     * 进行中预售活动列表
     */
    @GetMapping("/list")
    @Operation(summary = "进行中预售活动列表")
    public R<List<PresaleActivityVO>> list() {
        return R.success(presaleService.listOngoing());
    }

    /**
     * 预售活动详情（活动+商品信息）
     */
    @GetMapping("/{id}")
    @Operation(summary = "预售活动详情")
    public R<PresaleActivityVO> detail(@PathVariable Long id) {
        return R.success(presaleService.detail(id));
    }
}
