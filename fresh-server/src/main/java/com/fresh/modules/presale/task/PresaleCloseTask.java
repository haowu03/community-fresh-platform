package com.fresh.modules.presale.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.modules.presale.entity.PresaleActivity;
import com.fresh.modules.presale.mapper.PresaleActivityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预售到点关闭定时任务：每5分钟扫描一次
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresaleCloseTask {

    /** 活动状态：进行中 */
    private static final Integer STATUS_ONGOING = 1;
    /** 活动状态：已结束 */
    private static final Integer STATUS_FINISHED = 2;

    private final PresaleActivityMapper presaleActivityMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 把 end_time 已过且 status=1 的活动置为已结束，并删除对应Redis缓存
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void closeExpiredActivity() {
        List<PresaleActivity> expiredList = presaleActivityMapper.selectList(
                new LambdaQueryWrapper<PresaleActivity>()
                        .eq(PresaleActivity::getStatus, STATUS_ONGOING)
                        .lt(PresaleActivity::getEndTime, LocalDateTime.now()));
        if (expiredList.isEmpty()) {
            return;
        }
        for (PresaleActivity activity : expiredList) {
            activity.setStatus(STATUS_FINISHED);
            activity.setUpdateTime(LocalDateTime.now());
            presaleActivityMapper.updateById(activity);
            // 删除对应Redis key
            redisTemplate.delete(RedisCacheConstant.PRESALE_PREFIX + activity.getId());
        }
        log.info("预售到点关闭任务执行完成，共关闭 {} 个活动", expiredList.size());
    }
}
