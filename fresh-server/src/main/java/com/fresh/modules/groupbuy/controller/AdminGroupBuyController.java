package com.fresh.modules.groupbuy.controller;

import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.groupbuy.dto.GroupBuyPageQueryDTO;
import com.fresh.modules.groupbuy.service.GroupBuyService;
import com.fresh.modules.groupbuy.vo.GroupBuyMemberVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-团购活动管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/groupBuy")
@RequiredArgsConstructor
@Tag(name = "管理端-团购活动管理")
public class AdminGroupBuyController {

    private final GroupBuyService groupBuyService;

    /**
     * 团购活动分页（含进度）
     */
    @GetMapping("/page")
    @Operation(summary = "团购活动分页")
    public R<PageResult> page(GroupBuyPageQueryDTO dto) {
        log.info("团购活动分页: {}", dto);
        return R.success(groupBuyService.pageQuery(dto));
    }

    /**
     * 参团成员
     */
    @GetMapping("/{id}/members")
    @Operation(summary = "参团成员")
    public R<List<GroupBuyMemberVO>> members(@PathVariable Long id) {
        return R.success(groupBuyService.listMembers(id));
    }

    /**
     * 手动截团：进行中 → 达标置已成团，否则走流团逻辑
     */
    @PutMapping("/close/{id}")
    @Operation(summary = "手动截团")
    public R<Void> close(@PathVariable Long id) {
        log.info("手动截团: {}", id);
        groupBuyService.close(id);
        return R.success();
    }
}
