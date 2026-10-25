package com.fresh.modules.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团购活动实体
 */
@Data
@TableName("group_buy_activity")
public class GroupBuyActivity implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 团购商品id */
    private Long productId;

    /** 发起团长(user.id) */
    private Long leaderId;

    /** 统一配送自提点 */
    private Long pickupPointId;

    /** 团购价 */
    private BigDecimal groupPrice;

    /** 成团人数 */
    private Integer minNum;

    /** 已参团人数 */
    private Integer joinedNum;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 截止时间 */
    private LocalDateTime endTime;

    /** 状态 0未开始 1进行中 2已成团 3已完成 4已流团 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;
}
