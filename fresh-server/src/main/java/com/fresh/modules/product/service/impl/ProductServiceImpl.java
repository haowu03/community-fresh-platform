package com.fresh.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.common.constant.StatusConstant;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.common.result.PageResult;
import com.fresh.modules.category.entity.Category;
import com.fresh.modules.category.mapper.CategoryMapper;
import com.fresh.modules.product.dto.ProductDTO;
import com.fresh.modules.product.dto.ProductPageQueryDTO;
import com.fresh.modules.product.dto.TraceDTO;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.entity.ProductBatch;
import com.fresh.modules.product.entity.ProductTrace;
import com.fresh.modules.product.mapper.ProductMapper;
import com.fresh.modules.product.mapper.ProductTraceMapper;
import com.fresh.modules.product.service.ProductBatchService;
import com.fresh.modules.product.service.ProductService;
import com.fresh.modules.product.vo.BatchVO;
import com.fresh.modules.product.vo.NearExpiryProductVO;
import com.fresh.modules.product.vo.ProductDetailVO;
import com.fresh.modules.product.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 生鲜商品 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final CategoryMapper categoryMapper;
    private final ProductTraceMapper productTraceMapper;
    private final ProductBatchService productBatchService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult pageQuery(ProductPageQueryDTO queryDTO) {
        Page<Product> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Product> wrapper = buildWrapper(queryDTO)
                .orderByDesc(Product::getUpdateTime);
        this.page(page, wrapper);
        return new PageResult(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public PageResult pageQueryForUser(ProductPageQueryDTO queryDTO) {
        // 用户端只查起售商品
        queryDTO.setStatus(StatusConstant.ENABLE);
        Page<Product> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<Product> wrapper = buildWrapper(queryDTO)
                .orderByDesc(Product::getSaleCount)
                .orderByDesc(Product::getUpdateTime);
        this.page(page, wrapper);
        return new PageResult(page.getTotal(), toVOList(page.getRecords()));
    }

    @Override
    public ProductDetailVO getDetailById(Long id) {
        Product product = this.getProductOrThrow(id);
        ProductDetailVO vo = toDetailVO(product);
        // 管理端详情返回全部批次
        vo.setBatches(productBatchService.listByProductId(id));
        vo.setTrace(getTraceByProductId(id));
        return vo;
    }

    @Override
    public ProductDetailVO getUserDetailById(Long id) {
        Product product = this.getProductOrThrow(id);
        ProductDetailVO vo = toDetailVO(product);
        // 用户端详情只返回在售批次（正常/临期且有库存）
        vo.setBatches(productBatchService.listOnSaleByProductId(id));
        vo.setTrace(getTraceByProductId(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(ProductDTO productDTO) {
        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        product.setStatus(StatusConstant.ENABLE);
        product.setSaleCount(0);
        if (product.getStock() == null) {
            product.setStock(0);
        }
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());
        product.setCreateUser(BaseContext.getCurrentId());
        product.setUpdateUser(BaseContext.getCurrentId());
        this.save(product);

        // 填写了溯源字段时顺带 upsert 溯源信息
        upsertTraceFromDTO(productDTO, product.getId());

        clearProductCache(product.getId(), product.getCategoryId(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductDTO productDTO) {
        Product old = this.getProductOrThrow(productDTO.getId());

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);
        product.setUpdateTime(LocalDateTime.now());
        product.setUpdateUser(BaseContext.getCurrentId());
        this.updateById(product);

        upsertTraceFromDTO(productDTO, product.getId());

        // 分类可能变更，新旧分类的商品列表缓存都要清理
        Long oldCategoryId = java.util.Objects.equals(old.getCategoryId(), product.getCategoryId())
                ? null : old.getCategoryId();
        clearProductCache(product.getId(), product.getCategoryId(), oldCategoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        // 起售中的商品禁止删除
        Long onSaleCount = this.count(new LambdaQueryWrapper<Product>()
                .in(Product::getId, ids)
                .eq(Product::getStatus, StatusConstant.ENABLE));
        if (onSaleCount != null && onSaleCount > 0) {
            throw new BusinessException("存在起售中的商品，请先停售后再删除");
        }

        List<Product> products = this.listByIds(ids);
        this.removeByIds(ids);
        // 同步删除溯源信息
        productTraceMapper.delete(new LambdaQueryWrapper<ProductTrace>().in(ProductTrace::getProductId, ids));

        // 清理缓存：商品详情 + 涉及分类的商品列表 + 临期列表
        Set<Long> categoryIds = products.stream().map(Product::getCategoryId).collect(Collectors.toSet());
        Set<String> keys = new HashSet<>();
        ids.forEach(id -> keys.add(RedisCacheConstant.PRODUCT_DETAIL_PREFIX + id));
        categoryIds.forEach(cid -> keys.add(RedisCacheConstant.PRODUCT_LIST_PREFIX + cid));
        keys.add(RedisCacheConstant.NEAR_EXPIRY_LIST);
        redisTemplate.delete(keys);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Product product = this.getProductOrThrow(id);
        Product update = new Product();
        update.setId(id);
        update.setStatus(status);
        update.setUpdateTime(LocalDateTime.now());
        update.setUpdateUser(BaseContext.getCurrentId());
        this.updateById(update);
        clearProductCache(id, product.getCategoryId(), null);
    }

    @Override
    public List<Product> warnList() {
        // 库存低于预警值且起售中的商品
        return this.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, StatusConstant.ENABLE)
                .apply("stock <= warn_stock")
                .orderByAsc(Product::getStock));
    }

    @Override
    public void saveOrUpdateTrace(TraceDTO traceDTO) {
        this.getProductOrThrow(traceDTO.getProductId());
        ProductTrace trace = new ProductTrace();
        BeanUtils.copyProperties(traceDTO, trace);
        doUpsertTrace(trace);
        // 溯源变化影响商品详情缓存
        redisTemplate.delete(RedisCacheConstant.PRODUCT_DETAIL_PREFIX + traceDTO.getProductId());
    }

    @Override
    public ProductTrace getTraceByProductId(Long productId) {
        return productTraceMapper.selectOne(new LambdaQueryWrapper<ProductTrace>()
                .eq(ProductTrace::getProductId, productId));
    }

    @Override
    public List<NearExpiryProductVO> nearExpiryList() {
        // 临期且有库存的批次
        List<ProductBatch> batches = productBatchService.listNearExpiry();
        if (batches.isEmpty()) {
            return new ArrayList<>();
        }
        // 关联起售中的商品
        Set<Long> productIds = batches.stream().map(ProductBatch::getProductId).collect(Collectors.toSet());
        Map<Long, Product> productMap = this.list(new LambdaQueryWrapper<Product>()
                        .in(Product::getId, productIds)
                        .eq(Product::getStatus, StatusConstant.ENABLE))
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        Map<Long, String> categoryNameMap = getCategoryNameMap(
                productMap.values().stream().map(Product::getCategoryId).collect(Collectors.toSet()));

        LocalDate today = LocalDate.now();
        return batches.stream()
                .filter(batch -> productMap.containsKey(batch.getProductId()))
                .map(batch -> {
                    Product product = productMap.get(batch.getProductId());
                    NearExpiryProductVO vo = new NearExpiryProductVO();
                    BeanUtils.copyProperties(product, vo);
                    vo.setCategoryName(categoryNameMap.get(product.getCategoryId()));
                    vo.setFreshTypeDesc(freshTypeDesc(product.getFreshType()));
                    vo.setBatchId(batch.getId());
                    vo.setBatchNo(batch.getBatchNo());
                    vo.setBatchStock(batch.getStock());
                    vo.setDiscount(batch.getDiscount());
                    // 折后价 = 单价 × 折扣
                    vo.setDiscountPrice(product.getPrice().multiply(batch.getDiscount())
                            .setScale(2, RoundingMode.HALF_UP));
                    vo.setExpiryDate(batch.getExpiryDate());
                    vo.setRemainDays(ChronoUnit.DAYS.between(today, batch.getExpiryDate()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // ================= 私有辅助方法 =================

    /**
     * 构造商品查询条件（管理端/用户端共用）
     */
    private LambdaQueryWrapper<Product> buildWrapper(ProductPageQueryDTO queryDTO) {
        return new LambdaQueryWrapper<Product>()
                .like(StringUtils.hasText(queryDTO.getName()), Product::getName, queryDTO.getName())
                .eq(queryDTO.getCategoryId() != null, Product::getCategoryId, queryDTO.getCategoryId())
                .eq(queryDTO.getStatus() != null, Product::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getFreshType() != null, Product::getFreshType, queryDTO.getFreshType());
    }

    /**
     * 商品列表转 VO 列表，批量补齐分类名
     */
    private List<ProductVO> toVOList(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, String> categoryNameMap = getCategoryNameMap(
                products.stream().map(Product::getCategoryId).collect(Collectors.toSet()));
        return products.stream().map(product -> {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(product, vo);
            vo.setCategoryName(categoryNameMap.get(product.getCategoryId()));
            vo.setFreshTypeDesc(freshTypeDesc(product.getFreshType()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 商品转详情 VO（含分类名、保鲜类型描述、最新批次）
     */
    private ProductDetailVO toDetailVO(Product product) {
        ProductDetailVO vo = new ProductDetailVO();
        BeanUtils.copyProperties(product, vo);
        Category category = categoryMapper.selectById(product.getCategoryId());
        vo.setCategoryName(category == null ? null : category.getName());
        vo.setFreshTypeDesc(freshTypeDesc(product.getFreshType()));
        // 最新批次（最近创建）
        ProductBatch latest = productBatchService.getOne(new LambdaQueryWrapper<ProductBatch>()
                .eq(ProductBatch::getProductId, product.getId())
                .orderByDesc(ProductBatch::getCreateTime)
                .last("limit 1"));
        if (latest != null) {
            vo.setLatestBatch(BatchVO.of(latest));
        }
        return vo;
    }

    /**
     * 批量查询分类名
     */
    private Map<Long, String> getCategoryNameMap(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }

    /**
     * 保鲜类型中文描述
     */
    private String freshTypeDesc(Integer freshType) {
        if (freshType == null) {
            return null;
        }
        return switch (freshType) {
            case 0 -> "常温";
            case 1 -> "冷藏";
            case 2 -> "冷冻";
            default -> "未知";
        };
    }

    /**
     * 新增/修改商品时，若 DTO 中带有溯源字段则顺带 upsert 溯源信息
     */
    private void upsertTraceFromDTO(ProductDTO productDTO, Long productId) {
        boolean hasTrace = StringUtils.hasText(productDTO.getOriginPlace())
                || StringUtils.hasText(productDTO.getFarmer())
                || productDTO.getPlantDate() != null
                || productDTO.getPickDate() != null
                || StringUtils.hasText(productDTO.getTestReport())
                || StringUtils.hasText(productDTO.getTransportInfo());
        if (!hasTrace) {
            return;
        }
        ProductTrace trace = new ProductTrace();
        trace.setProductId(productId);
        trace.setOriginPlace(productDTO.getOriginPlace());
        trace.setFarmer(productDTO.getFarmer());
        trace.setPlantDate(productDTO.getPlantDate());
        trace.setPickDate(productDTO.getPickDate());
        trace.setTestReport(productDTO.getTestReport());
        trace.setTransportInfo(productDTO.getTransportInfo());
        doUpsertTrace(trace);
    }

    /**
     * 按 product_id 唯一约束 upsert 溯源信息
     */
    private void doUpsertTrace(ProductTrace trace) {
        ProductTrace existing = productTraceMapper.selectOne(new LambdaQueryWrapper<ProductTrace>()
                .eq(ProductTrace::getProductId, trace.getProductId()));
        if (existing != null) {
            trace.setId(existing.getId());
            productTraceMapper.updateById(trace);
        } else {
            trace.setCreateTime(LocalDateTime.now());
            productTraceMapper.insert(trace);
        }
    }

    /**
     * 清理商品相关缓存：详情、分类商品列表（含旧分类）、临期特价列表
     */
    private void clearProductCache(Long productId, Long categoryId, Long oldCategoryId) {
        Set<String> keys = new HashSet<>();
        keys.add(RedisCacheConstant.PRODUCT_DETAIL_PREFIX + productId);
        if (categoryId != null) {
            keys.add(RedisCacheConstant.PRODUCT_LIST_PREFIX + categoryId);
        }
        if (oldCategoryId != null) {
            keys.add(RedisCacheConstant.PRODUCT_LIST_PREFIX + oldCategoryId);
        }
        keys.add(RedisCacheConstant.NEAR_EXPIRY_LIST);
        redisTemplate.delete(keys);
    }

    /**
     * 查询商品，不存在则抛业务异常
     */
    private Product getProductOrThrow(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }
}
