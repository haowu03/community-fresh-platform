package com.fresh.modules.groupbuy.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.modules.groupbuy.entity.GroupBuyActivity;
import com.fresh.modules.groupbuy.service.GroupBuyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 团购到期结算定时任务：每5分钟扫描一次
 * 成团：置已成团并通知团长，订单进入正常履约
 * 流团：批量取消订单、模拟退款、成员置已退款并逐个通知（单个活动一个事务）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupBuySettleTask {

    /** 活动状态：进行中 */
    private static final Integer STATUS_ONGOING = 1;

    private final GroupBuyService groupBuyService;

    /**
     * 扫描 status=1 且 end_time 已过的活动并逐个结算
     * 结算逻辑在 GroupBuyService.settleOne 中，带 @Transactional 独立事务
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void settle() {
        List<GroupBuyActivity> expiredList = groupBuyService.list(
                new LambdaQueryWrapper<GroupBuyActivity>()
                        .eq(GroupBuyActivity::getStatus, STATUS_ONGOING)
                        .lt(GroupBuyActivity::getEndTime, LocalDateTime.now()));
        if (expiredList.isEmpty()) {
            return;
        }
        log.info("团购到期结算任务开始，待结算活动 {} 个", expiredList.size());
        for (GroupBuyActivity activity : expiredList) {
            try {
                // 跨Bean调用，单个活动独立事务，失败互不影响
                groupBuyService.settleOne(activity);
            } catch (Exception e) {
                log.error("团购活动结算失败: activityId={}", activity.getId(), e);
            }
        }
        log.info("团购到期结算任务执行完成");
    }
}
