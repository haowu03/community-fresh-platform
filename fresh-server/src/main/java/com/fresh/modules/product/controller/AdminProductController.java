package com.fresh.modules.product.controller;

import com.fresh.common.result.PageResult;
import com.fresh.common.result.R;
import com.fresh.modules.product.dto.BatchStockInDTO;
import com.fresh.modules.product.dto.ProductDTO;
import com.fresh.modules.product.dto.ProductPageQueryDTO;
import com.fresh.modules.product.dto.TraceDTO;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.service.ProductBatchService;
import com.fresh.modules.product.service.ProductService;
import com.fresh.modules.product.vo.BatchVO;
import com.fresh.modules.product.vo.ProductDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端-生鲜商品管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/product")
@RequiredArgsConstructor
@Tag(name = "管理端-商品管理")
public class AdminProductController {

    private final ProductService productService;
    private final ProductBatchService productBatchService;

    /**
     * 商品分页查询（VO 带分类名）
     */
    @GetMapping("/page")
    @Operation(summary = "商品分页查询")
    public R<PageResult> page(ProductPageQueryDTO queryDTO) {
        log.info("商品分页查询：{}", queryDTO);
        return R.success(productService.pageQuery(queryDTO));
    }

    /**
     * 商品详情（含全部批次、溯源）
     */
    @GetMapping("/{id}")
    @Operation(summary = "商品详情")
    public R<ProductDetailVO> getById(@PathVariable Long id) {
        return R.success(productService.getDetailById(id));
    }

    /**
     * 新增商品（可顺带维护溯源信息）
     */
    @PostMapping
    @Operation(summary = "新增商品")
    public R<Void> save(@RequestBody ProductDTO productDTO) {
        log.info("新增商品：{}", productDTO);
        productService.saveProduct(productDTO);
        return R.success();
    }

    /**
     * 修改商品
     */
    @PutMapping
    @Operation(summary = "修改商品")
    public R<Void> update(@RequestBody ProductDTO productDTO) {
        log.info("修改商品：{}", productDTO);
        productService.updateProduct(productDTO);
        return R.success();
    }

    /**
     * 批量删除商品（起售中禁止删除）
     */
    @DeleteMapping
    @Operation(summary = "批量删除商品")
    public R<Void> delete(@RequestParam List<Long> ids) {
        log.info("批量删除商品：{}", ids);
        productService.deleteBatch(ids);
        return R.success();
    }

    /**
     * 起售/停售
     */
    @PutMapping("/status/{status}")
    @Operation(summary = "起售停售")
    public R<Void> startOrStop(@PathVariable Integer status, @RequestParam Long id) {
        log.info("商品启停：status={}, id={}", status, id);
        productService.startOrStop(status, id);
        return R.success();
    }

    /**
     * 库存预警列表
     */
    @GetMapping("/warnList")
    @Operation(summary = "库存预警列表")
    public R<List<Product>> warnList() {
        return R.success(productService.warnList());
    }

    /**
     * 批次入库（商品库存累加并同步 Redis）
     */
    @PostMapping("/batch")
    @Operation(summary = "批次入库")
    public R<Void> stockIn(@RequestBody BatchStockInDTO stockInDTO) {
        log.info("批次入库：{}", stockInDTO);
        productBatchService.stockIn(stockInDTO);
        return R.success();
    }

    /**
     * 商品批次列表
     */
    @GetMapping("/batch/list")
    @Operation(summary = "商品批次列表")
    public R<List<BatchVO>> batchList(@RequestParam Long productId) {
        return R.success(productBatchService.listByProductId(productId));
    }

    /**
     * 维护溯源信息（存在即更新）
     */
    @PutMapping("/trace")
    @Operation(summary = "维护溯源信息")
    public R<Void> saveTrace(@RequestBody TraceDTO traceDTO) {
        log.info("维护溯源信息：{}", traceDTO);
        productService.saveOrUpdateTrace(traceDTO);
        return R.success();
    }
}
