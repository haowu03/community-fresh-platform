package com.fresh.modules.loss.task;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.modules.product.entity.ProductBatch;
import com.fresh.modules.product.mapper.ProductBatchMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 批次临期/过期扫描定时任务单元测试
 */
@ExtendWith(MockitoExtension.class)
class BatchExpiryTaskTest {

    @Mock
    private ProductBatchMapper productBatchMapper;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private BatchExpiryTask batchExpiryTask;

    @Test
    @DisplayName("过期批次置为过期，临期批次自动7折，并清理临期缓存")
    void processBatchExpiry_marksExpiredAndDiscountsNearExpiry() {
        LocalDate today = LocalDate.now();

        // 已过到期日的正常批次 -> 应为过期(3)
        ProductBatch expired = new ProductBatch();
        expired.setId(1L);
        expired.setStatus(ProductBatch.STATUS_NORMAL);
        expired.setExpiryDate(today.minusDays(1));

        // 2 天内到期的正常批次 -> 应为临期(2) + 7折
        ProductBatch nearExpiry = new ProductBatch();
        nearExpiry.setId(2L);
        nearExpiry.setStatus(ProductBatch.STATUS_NORMAL);
        nearExpiry.setExpiryDate(today.plusDays(2));
        nearExpiry.setDiscount(BigDecimal.ONE);

        // 第一次 selectList 返回过期批次，第二次返回临期批次（任务内两次查询顺序固定）
        when(productBatchMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(expired), List.of(nearExpiry));

        List<ProductBatch> updated = new ArrayList<>();
        doAnswer(invocation -> {
            updated.add(invocation.getArgument(0));
            return 1;
        }).when(productBatchMapper).updateById(any(ProductBatch.class));

        batchExpiryTask.processBatchExpiry();

        assertEquals(2, updated.size(), "应更新两个批次");
        assertEquals(ProductBatch.STATUS_EXPIRED, updated.get(0).getStatus(),
                "已过到期日的批次应置为过期");
        assertEquals(ProductBatch.STATUS_NEAR_EXPIRY, updated.get(1).getStatus(),
                "2天内到期的批次应置为临期");
        assertEquals(new BigDecimal("0.70"), updated.get(1).getDiscount(), "临期应自动打7折");

        verify(redisTemplate).delete(RedisCacheConstant.NEAR_EXPIRY_LIST);
    }

    @Test
    @DisplayName("无过期无临期批次：不更新任何批次，仅清理缓存")
    void processBatchExpiry_nothingToDo() {
        when(productBatchMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(), List.of());

        batchExpiryTask.processBatchExpiry();

        verify(productBatchMapper, never()).updateById(any(ProductBatch.class));
        verify(redisTemplate).delete(RedisCacheConstant.NEAR_EXPIRY_LIST);
    }
}