package com.fresh.modules.product.controller;

import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.product.dto.ProductPageQueryDTO;
import com.fresh.modules.product.entity.ProductTrace;
import com.fresh.modules.product.service.ProductService;
import com.fresh.modules.product.vo.NearExpiryProductVO;
import com.fresh.modules.product.vo.ProductDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户端-生鲜商品浏览
 */
@Slf4j
@RestController
@RequestMapping("/user/product")
@RequiredArgsConstructor
@Tag(name = "用户端-商品浏览")
public class UserProductController {

    /** 商品详情缓存时长：30 分钟 */
    private static final long DETAIL_CACHE_MINUTES = 30L;

    /** 临期特价缓存时长：10 分钟 */
    private static final long NEAR_EXPIRY_CACHE_MINUTES = 10L;

    private final ProductService productService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 商品分页查询（只查起售商品；按分类查首页时走 Redis 缓存，名称搜索不缓存）
     */
    @GetMapping("/list")
    @Operation(summary = "商品分页查询")
    public R<PageResult> list(@RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) String name,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer pageSize) {
        // 按分类浏览（首页场景）走缓存
        boolean cacheable = categoryId != null && !StringUtils.hasText(name);
        String cacheKey = cacheable ? RedisCacheConstant.PRODUCT_LIST_PREFIX + categoryId : null;
        if (cacheable) {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return R.success((PageResult) cached);
            }
        }

        ProductPageQueryDTO queryDTO = new ProductPageQueryDTO();
        queryDTO.setCategoryId(categoryId);
        queryDTO.setName(name);
        queryDTO.setPage(page);
        queryDTO.setPageSize(pageSize);
        PageResult pageResult = productService.pageQueryForUser(queryDTO);

        if (cacheable) {
            redisTemplate.opsForValue().set(cacheKey, pageResult);
        }
        return R.success(pageResult);
    }

    /**
     * 商品详情（商品 + 溯源 + 在售批次，缓存 30 分钟）
     */
    @GetMapping("/{id}")
    @Operation(summary = "商品详情")
    public R<ProductDetailVO> getById(@PathVariable Long id) {
        String cacheKey = RedisCacheConstant.PRODUCT_DETAIL_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return R.success((ProductDetailVO) cached);
        }
        ProductDetailVO detail = productService.getUserDetailById(id);
        redisTemplate.opsForValue().set(cacheKey, detail, DETAIL_CACHE_MINUTES, TimeUnit.MINUTES);
        return R.success(detail);
    }

    /**
     * 临期特价专区（缓存 10 分钟）
     */
    @GetMapping("/nearExpiry")
    @Operation(summary = "临期特价专区")
    @SuppressWarnings("unchecked")
    public R<List<NearExpiryProductVO>> nearExpiry() {
        Object cached = redisTemplate.opsForValue().get(RedisCacheConstant.NEAR_EXPIRY_LIST);
        if (cached != null) {
            return R.success((List<NearExpiryProductVO>) cached);
        }
        List<NearExpiryProductVO> list = productService.nearExpiryList();
        redisTemplate.opsForValue().set(RedisCacheConstant.NEAR_EXPIRY_LIST, list,
                NEAR_EXPIRY_CACHE_MINUTES, TimeUnit.MINUTES);
        return R.success(list);
    }

    /**
     * 商品溯源信息
     */
    @GetMapping("/{id}/trace")
    @Operation(summary = "商品溯源信息")
    public R<ProductTrace> trace(@PathVariable Long id) {
        return R.success(productService.getTraceByProductId(id));
    }
}
