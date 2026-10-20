package com.fresh.modules.loss.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fresh.common.result.PageResult;
import com.fresh.modules.loss.service.LossService;
import com.fresh.modules.loss.vo.ProductBatchVO;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.entity.ProductBatch;
import com.fresh.modules.product.mapper.ProductBatchMapper;
import com.fresh.modules.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 损耗/临期批次管理 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LossServiceImpl implements LossService {

    private final ProductBatchMapper productBatchMapper;
    private final ProductMapper productMapper;

    @Override
    public PageResult batchPage(Integer status, Integer page, Integer pageSize) {
        Page<ProductBatch> batchPage = new Page<>(page == null ? 1 : page, pageSize == null ? 10 : pageSize);
        productBatchMapper.selectPage(batchPage, new LambdaQueryWrapper<ProductBatch>()
                .eq(status != null, ProductBatch::getStatus, status)
                .orderByAsc(ProductBatch::getExpiryDate));

        List<ProductBatch> records = batchPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            return new PageResult(batchPage.getTotal(), Collections.emptyList());
        }

        // 批量查商品名，避免N+1
        List<Long> productIds = records.stream()
                .map(ProductBatch::getProductId).distinct().collect(Collectors.toList());
        Map<Long, String> productNameMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Product::getName));

        List<ProductBatchVO> voList = records.stream().map(batch -> {
            ProductBatchVO vo = BeanUtil.copyProperties(batch, ProductBatchVO.class);
            vo.setProductName(productNameMap.get(batch.getProductId()));
            vo.setStatusDesc(switch (batch.getStatus() == null ? -1 : batch.getStatus()) {
                case 1 -> "正常";
                case 2 -> "临期";
                case 3 -> "过期";
                default -> "未知";
            });
            return vo;
        }).collect(Collectors.toList());
        return new PageResult(batchPage.getTotal(), voList);
    }
}
