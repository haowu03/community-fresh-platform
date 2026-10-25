package com.fresh.modules.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.common.result.PageResult;
import com.fresh.modules.product.dto.ProductDTO;
import com.fresh.modules.product.dto.ProductPageQueryDTO;
import com.fresh.modules.product.dto.TraceDTO;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.entity.ProductTrace;
import com.fresh.modules.product.vo.NearExpiryProductVO;
import com.fresh.modules.product.vo.ProductDetailVO;

import java.util.List;

/**
 * 生鲜商品 Service
 */
public interface ProductService extends IService<Product> {

    /**
     * 管理端商品分页查询（VO 带分类名）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQuery(ProductPageQueryDTO queryDTO);

    /**
     * 用户端商品分页查询（只查起售商品）
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResult pageQueryForUser(ProductPageQueryDTO queryDTO);

    /**
     * 商品详情（商品 + 溯源 + 批次列表）
     *
     * @param id 商品id
     * @return 详情
     */
    ProductDetailVO getDetailById(Long id);

    /**
     * 用户端商品详情（商品 + 溯源 + 在售批次）
     *
     * @param id 商品id
     * @return 详情
     */
    ProductDetailVO getUserDetailById(Long id);

    /**
     * 新增商品（有溯源信息时顺带 upsert 溯源）
     *
     * @param productDTO 商品信息
     */
    void saveProduct(ProductDTO productDTO);

    /**
     * 修改商品
     *
     * @param productDTO 商品信息
     */
    void updateProduct(ProductDTO productDTO);

    /**
     * 批量删除商品（起售中的商品禁止删除）
     *
     * @param ids 商品id集合
     */
    void deleteBatch(List<Long> ids);

    /**
     * 起售/停售
     *
     * @param status 状态
     * @param id     商品id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 库存预警列表（stock &lt;= warn_stock 且起售中）
     *
     * @return 预警商品列表
     */
    List<Product> warnList();

    /**
     * 维护溯源信息（product_id 唯一，存在即更新）
     *
     * @param traceDTO 溯源信息
     */
    void saveOrUpdateTrace(TraceDTO traceDTO);

    /**
     * 查询商品溯源信息
     *
     * @param productId 商品id
     * @return 溯源信息
     */
    ProductTrace getTraceByProductId(Long productId);

    /**
     * 临期特价专区列表
     *
     * @return 临期特价商品列表
     */
    List<NearExpiryProductVO> nearExpiryList();
}
