package com.fresh.modules.product.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商品溯源实体（product_id 唯一）
 */
@Data
@TableName("product_trace")
public class ProductTrace implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品id */
    private Long productId;

    /** 产地 */
    private String originPlace;

    /** 种植户/养殖户 */
    private String farmer;

    /** 种植日期 */
    private LocalDate plantDate;

    /** 采摘/出栏日期 */
    private LocalDate pickDate;

    /** 检测报告(图片url) */
    private String testReport;

    /** 运输/冷链信息 */
    private String transportInfo;

    /** 创建时间 */
    private LocalDateTime createTime;
}
