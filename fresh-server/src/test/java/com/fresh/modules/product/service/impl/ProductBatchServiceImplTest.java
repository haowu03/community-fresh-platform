package com.fresh.modules.product.service.impl;

import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.common.exception.BusinessException;
import com.fresh.modules.product.dto.BatchStockInDTO;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.entity.ProductBatch;
import com.fresh.modules.product.mapper.ProductBatchMapper;
import com.fresh.modules.product.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 商品批次入库单元测试：到期日计算、库存累加与缓存同步
 */
@ExtendWith(MockitoExtension.class)
class ProductBatchServiceImplTest {

    @Mock
    private ProductMapper productMapper;
    @Mock
    private ProductBatchMapper productBatchMapper;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private ProductBatchServiceImpl productBatchService;

    @BeforeEach
    void setUp() {
        // Mockito 不注入父类 ServiceImpl 的 baseMapper，手动注入 mock
        ReflectionTestUtils.setField(productBatchService, "baseMapper", productBatchMapper);
    }

    @Test
    @DisplayName("商品不存在：拒绝入库")
    void stockIn_productNotFound() {
        when(productMapper.selectById(1L)).thenReturn(null);

        BatchStockInDTO dto = new BatchStockInDTO();
        dto.setProductId(1L);
        assertThrows(BusinessException.class, () -> productBatchService.stockIn(dto));
    }

    @Test
    @DisplayName("入库数量不合法：拒绝入库")
    void stockIn_invalidStock() {
        Product product = new Product();
        product.setId(1L);
        when(productMapper.selectById(1L)).thenReturn(product);

        BatchStockInDTO dto = new BatchStockInDTO();
        dto.setProductId(1L);
        dto.setStock(0);
        BusinessException e = assertThrows(BusinessException.class, () -> productBatchService.stockIn(dto));
        assertEquals("入库数量必须大于0", e.getMessage());
    }

    @Test
    @DisplayName("商品未设置保质期：拒绝入库")
    void stockIn_noShelfLife() {
        Product product = new Product();
        product.setId(1L);
        product.setShelfLifeDays(null);
        when(productMapper.selectById(1L)).thenReturn(product);

        BatchStockInDTO dto = new BatchStockInDTO();
        dto.setProductId(1L);
        dto.setStock(10);
        BusinessException e = assertThrows(BusinessException.class, () -> productBatchService.stockIn(dto));
        assertEquals("该商品未设置保质期，无法批次入库", e.getMessage());
    }

    @Test
    @DisplayName("入库成功：到期日=生产日期+保质期，商品总库存累加并同步缓存")
    void stockIn_success() {
        Product product = new Product();
        product.setId(1L);
        product.setCategoryId(2L);
        product.setStock(30);
        product.setShelfLifeDays(3);
        when(productMapper.selectById(1L)).thenReturn(product);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        BatchStockInDTO dto = new BatchStockInDTO();
        dto.setProductId(1L);
        dto.setProductionDate(LocalDate.of(2026, 10, 20));
        dto.setStock(10);

        List<ProductBatch> savedBatch = new ArrayList<>();
        doAnswer(invocation -> {
            savedBatch.add(invocation.getArgument(0));
            return 1;
        }).when(productBatchMapper).insert(any(ProductBatch.class));

        List<Product> updatedProduct = new ArrayList<>();
        doAnswer(invocation -> {
            updatedProduct.add(invocation.getArgument(0));
            return 1;
        }).when(productMapper).updateById(any(Product.class));

        productBatchService.stockIn(dto);

        ProductBatch saved = savedBatch.get(0);
        assertEquals(LocalDate.of(2026, 10, 23), saved.getExpiryDate(),
                "到期日应为生产日期 + 保质期天数");
        assertEquals(10, saved.getStock());
        assertEquals(10, saved.getInitStock());
        assertEquals(ProductBatch.STATUS_NORMAL, saved.getStatus());

        assertEquals(40, updatedProduct.get(0).getStock(), "商品总库存应累加");

        verify(valueOperations).set(RedisCacheConstant.PRODUCT_STOCK_PREFIX + 1L, 40);
        verify(redisTemplate).delete(RedisCacheConstant.PRODUCT_DETAIL_PREFIX + 1L);
        verify(redisTemplate).delete(RedisCacheConstant.PRODUCT_LIST_PREFIX + 2L);
        verify(redisTemplate).delete(RedisCacheConstant.NEAR_EXPIRY_LIST);
    }
}