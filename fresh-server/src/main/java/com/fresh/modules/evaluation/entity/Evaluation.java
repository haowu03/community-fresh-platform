package com.fresh.modules.evaluation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品评价实体
 */
@Data
@TableName("evaluation")
public class Evaluation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单id */
    private Long orderId;

    /** 用户id */
    private Long userId;

    /** 商品id */
    private Long productId;

    /** 评分 1-5 */
    private Integer score;

    /** 评价内容 */
    private String content;

    /** 评价图片(JSON数组) */
    private String images;

    /** 评价时间 */
    private LocalDateTime createTime;
}
