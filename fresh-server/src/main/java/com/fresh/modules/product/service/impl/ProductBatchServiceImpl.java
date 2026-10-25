package com.fresh.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.common.exception.BusinessException;
import com.fresh.modules.product.dto.BatchStockInDTO;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.entity.ProductBatch;
import com.fresh.modules.product.mapper.ProductBatchMapper;
import com.fresh.modules.product.mapper.ProductMapper;
import com.fresh.modules.product.service.ProductBatchService;
import com.fresh.modules.product.vo.BatchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品批次 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductBatchServiceImpl extends ServiceImpl<ProductBatchMapper, ProductBatch> implements ProductBatchService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<BatchVO> listByProductId(Long productId) {
        // 先到期先出（FEFO）
        List<ProductBatch> batches = this.list(new LambdaQueryWrapper<ProductBatch>()
                .eq(ProductBatch::getProductId, productId)
                .orderByAsc(ProductBatch::getExpiryDate));
        return batches.stream().map(BatchVO::of).collect(Collectors.toList());
    }

    @Override
    public List<BatchVO> listOnSaleByProductId(Long productId) {
        List<ProductBatch> batches = this.list(new LambdaQueryWrapper<ProductBatch>()
                .eq(ProductBatch::getProductId, productId)
                .in(ProductBatch::getStatus, ProductBatch.STATUS_NORMAL, ProductBatch.STATUS_NEAR_EXPIRY)
                .gt(ProductBatch::getStock, 0)
                .orderByAsc(ProductBatch::getExpiryDate));
        return batches.stream().map(BatchVO::of).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockIn(BatchStockInDTO stockInDTO) {
        Product product = productMapper.selectById(stockInDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (stockInDTO.getStock() == null || stockInDTO.getStock() <= 0) {
            throw new BusinessException("入库数量必须大于0");
        }
        if (product.getShelfLifeDays() == null) {
            throw new BusinessException("该商品未设置保质期，无法批次入库");
        }

        // 到期日 = 生产日期 + 保质期天数
        LocalDate expiryDate = stockInDTO.getProductionDate().plusDays(product.getShelfLifeDays());

        ProductBatch batch = new ProductBatch();
        batch.setProductId(product.getId());
        // 批次号：B + 时间戳
        batch.setBatchNo("B" + System.currentTimeMillis());
        batch.setProductionDate(stockInDTO.getProductionDate());
        batch.setExpiryDate(expiryDate);
        batch.setStock(stockInDTO.getStock());
        batch.setInitStock(stockInDTO.getStock());
        batch.setDiscount(BigDecimal.ONE);
        batch.setStatus(ProductBatch.STATUS_NORMAL);
        batch.setCreateTime(LocalDateTime.now());
        batch.setUpdateTime(LocalDateTime.now());
        this.save(batch);

        // 商品总库存累加
        int newStock = (product.getStock() == null ? 0 : product.getStock()) + stockInDTO.getStock();
        Product update = new Product();
        update.setId(product.getId());
        update.setStock(newStock);
        update.setUpdateTime(LocalDateTime.now());
        productMapper.updateById(update);

        // 同步 Redis 库存 key，并清理相关缓存
        redisTemplate.opsForValue().set(RedisCacheConstant.PRODUCT_STOCK_PREFIX + product.getId(), newStock);
        redisTemplate.delete(RedisCacheConstant.PRODUCT_DETAIL_PREFIX + product.getId());
        redisTemplate.delete(RedisCacheConstant.PRODUCT_LIST_PREFIX + product.getCategoryId());
        redisTemplate.delete(RedisCacheConstant.NEAR_EXPIRY_LIST);
        log.info("批次入库完成：batchNo={}, productId={}, stock={}", batch.getBatchNo(), product.getId(), newStock);
    }

    @Override
    public List<ProductBatch> listNearExpiry() {
        return this.list(new LambdaQueryWrapper<ProductBatch>()
                .eq(ProductBatch::getStatus, ProductBatch.STATUS_NEAR_EXPIRY)
                .gt(ProductBatch::getStock, 0)
                .orderByAsc(ProductBatch::getExpiryDate));
    }
}
