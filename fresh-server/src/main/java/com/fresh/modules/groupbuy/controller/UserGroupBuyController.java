package com.fresh.modules.groupbuy.controller;

import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.common.result.R;
import com.fresh.modules.groupbuy.dto.GroupBuyLaunchDTO;
import com.fresh.modules.groupbuy.entity.GroupBuyActivity;
import com.fresh.modules.groupbuy.service.GroupBuyService;
import com.fresh.modules.groupbuy.vo.GroupBuyActivityVO;
import com.fresh.modules.groupbuy.vo.GroupBuyMemberVO;
import com.fresh.modules.order.dto.OrderVerifyDTO;
import com.fresh.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端-社区团购（用户 + 团长）
 */
@Slf4j
@RestController
@RequestMapping("/user/groupBuy")
@RequiredArgsConstructor
@Tag(name = "用户端-社区团购")
public class UserGroupBuyController {

    private final GroupBuyService groupBuyService;
    private final OrderService orderService;

    /**
     * 进行中团购列表
     */
    @GetMapping("/list")
    @Operation(summary = "进行中团购列表")
    public R<List<GroupBuyActivityVO>> list() {
        return R.success(groupBuyService.listOngoing());
    }

    /**
     * 团购详情（含参团进度）
     */
    @GetMapping("/{id}")
    @Operation(summary = "团购详情")
    public R<GroupBuyActivityVO> detail(@PathVariable Long id) {
        return R.success(groupBuyService.detail(id));
    }

    /**
     * 我参的团
     */
    @GetMapping("/myJoin")
    @Operation(summary = "我参的团")
    public R<List<GroupBuyActivityVO>> myJoin() {
        return R.success(groupBuyService.myJoin());
    }

    /**
     * 发起团购（仅团长）
     */
    @PostMapping
    @Operation(summary = "发起团购（团长）")
    public R<Void> launch(@RequestBody GroupBuyLaunchDTO dto) {
        log.info("发起团购: {}", dto);
        groupBuyService.launch(dto);
        return R.success();
    }

    /**
     * 我发起的团购（团长）
     */
    @GetMapping("/myLaunch")
    @Operation(summary = "我发起的团购（团长）")
    public R<List<GroupBuyActivityVO>> myLaunch() {
        return R.success(groupBuyService.myLaunch());
    }

    /**
     * 参团成员（仅发起该团的团长可见）
     */
    @GetMapping("/{id}/members")
    @Operation(summary = "参团成员（团长）")
    public R<List<GroupBuyMemberVO>> members(@PathVariable Long id) {
        GroupBuyActivity activity = groupBuyService.getById(id);
        if (activity == null) {
            throw new BusinessException("团购活动不存在");
        }
        // 简单校验：仅活动发起团长可见
        if (!activity.getLeaderId().equals(BaseContext.getCurrentId())) {
            throw new BusinessException("仅发起团长可查看参团成员");
        }
        return R.success(groupBuyService.listMembers(id));
    }

    /**
     * 团长核销自提码（本自提点订单）
     */
    @PutMapping("/verify")
    @Operation(summary = "团长核销自提码")
    public R<Void> verify(@RequestBody OrderVerifyDTO dto) {
        log.info("团长核销自提码: {}", dto.getPickupCode());
        orderService.verify(dto, BaseContext.getCurrentId(), false);
        return R.success();
    }
}
