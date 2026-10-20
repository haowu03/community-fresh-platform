package com.fresh.modules.community.controller;

import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.community.dto.PostAuditDTO;
import com.fresh.modules.community.service.CommunityPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端-社区帖子审核
 */
@Slf4j
@RestController
@RequestMapping("/admin/post")
@RequiredArgsConstructor
@Tag(name = "管理端-社区帖子审核")
public class AdminPostController {

    private final CommunityPostService communityPostService;

    /**
     * 帖子分页（审核列表）
     */
    @GetMapping("/page")
    @Operation(summary = "帖子分页（审核列表）")
    public R<PageResult> page(@RequestParam(required = false) Integer status,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("帖子审核分页: status={}, page={}, pageSize={}", status, page, pageSize);
        return R.success(communityPostService.adminPage(status, page, pageSize));
    }

    /**
     * 审核：通过/拒绝
     */
    @PutMapping("/audit")
    @Operation(summary = "审核帖子")
    public R<Void> audit(@RequestBody PostAuditDTO dto) {
        log.info("审核帖子: {}", dto);
        communityPostService.audit(dto);
        return R.success();
    }
}
