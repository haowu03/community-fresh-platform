package com.fresh.modules.loss.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.modules.product.entity.ProductBatch;
import com.fresh.modules.product.mapper.ProductBatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 批次临期/过期处理定时任务：每日 00:30 执行
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchExpiryTask {

    /** 批次状态：正常 */
    private static final Integer BATCH_NORMAL = 1;
    /** 批次状态：临期 */
    private static final Integer BATCH_NEAR_EXPIRY = 2;
    /** 批次状态：过期 */
    private static final Integer BATCH_EXPIRED = 3;

    /** 临期阈值：距到期日 <= 2 天 */
    private static final int NEAR_EXPIRY_DAYS = 2;
    /** 临期自动折扣：7折 */
    private static final BigDecimal NEAR_EXPIRY_DISCOUNT = new BigDecimal("0.70");

    private final ProductBatchMapper productBatchMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * a) 过期批次置 status=3（库存保留记录用于损耗报表，不动 product.stock）
     * b) 临期批次置 status=2 并自动打7折
     * c) 处理完删除临期特价缓存
     */
    @Scheduled(cron = "0 30 0 * * ?")
    public void processBatchExpiry() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // a) 过期：status != 3 且 expiry_date < 今天
        List<ProductBatch> expiredList = productBatchMapper.selectList(
                new LambdaQueryWrapper<ProductBatch>()
                        .ne(ProductBatch::getStatus, BATCH_EXPIRED)
                        .lt(ProductBatch::getExpiryDate, today));
        for (ProductBatch batch : expiredList) {
            batch.setStatus(BATCH_EXPIRED);
            batch.setUpdateTime(now);
            productBatchMapper.updateById(batch);
        }

        // b) 临期：status=1 且 expiry_date - 今天 <= 2天（含今天到期）
        List<ProductBatch> nearExpiryList = productBatchMapper.selectList(
                new LambdaQueryWrapper<ProductBatch>()
                        .eq(ProductBatch::getStatus, BATCH_NORMAL)
                        .le(ProductBatch::getExpiryDate, today.plusDays(NEAR_EXPIRY_DAYS)));
        for (ProductBatch batch : nearExpiryList) {
            batch.setStatus(BATCH_NEAR_EXPIRY);
            batch.setDiscount(NEAR_EXPIRY_DISCOUNT);
            batch.setUpdateTime(now);
            productBatchMapper.updateById(batch);
        }

        // c) 删除临期特价列表缓存，等待下次访问重建
        redisTemplate.delete(RedisCacheConstant.NEAR_EXPIRY_LIST);

        // d) 统计日志
        log.info("批次临期/过期处理完成：过期 {} 个批次，临期打折 {} 个批次",
                expiredList.size(), nearExpiryList.size());
    }
}
