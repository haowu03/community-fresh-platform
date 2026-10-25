package com.fresh.modules.presale.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预售活动实体
 */
@Data
@TableName("presale_activity")
public class PresaleActivity implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 预售商品id */
    private Long productId;

    /** 预售价 */
    private BigDecimal presalePrice;

    /** 预售限购库存 */
    private Integer stockLimit;

    /** 已售数量 */
    private Integer soldCount;

    /** 活动开始时间 */
    private LocalDateTime startTime;

    /** 下单截止时间 */
    private LocalDateTime endTime;

    /** 统一发货日期 */
    private LocalDate deliveryDate;

    /** 状态 0未开始 1进行中 2已结束 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;
}
