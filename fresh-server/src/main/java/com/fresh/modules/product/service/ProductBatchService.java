package com.fresh.modules.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.modules.product.dto.BatchStockInDTO;
import com.fresh.modules.product.entity.ProductBatch;
import com.fresh.modules.product.vo.BatchVO;

import java.util.List;

/**
 * 商品批次 Service
 */
public interface ProductBatchService extends IService<ProductBatch> {

    /**
     * 按商品查询全部批次（按到期日升序，先到期先出）
     *
     * @param productId 商品id
     * @return 批次列表
     */
    List<BatchVO> listByProductId(Long productId);

    /**
     * 按商品查询在售批次（正常/临期且有库存）
     *
     * @param productId 商品id
     * @return 批次列表
     */
    List<BatchVO> listOnSaleByProductId(Long productId);

    /**
     * 批次入库：创建批次，商品总库存累加并同步 Redis
     *
     * @param stockInDTO 入库信息
     */
    void stockIn(BatchStockInDTO stockInDTO);

    /**
     * 查询临期且有库存的批次
     *
     * @return 临期批次列表
     */
    List<ProductBatch> listNearExpiry();
}
