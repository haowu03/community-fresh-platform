package com.fresh.modules.groupbuy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 参团记录实体（activity_id + user_id 唯一约束）
 */
@Data
@TableName("group_buy_member")
public class GroupBuyMember implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 团购活动id */
    private Long activityId;

    /** 参团用户id */
    private Long userId;

    /** 关联订单id */
    private Long orderId;

    /** 状态 1已支付 2已退款 */
    private Integer status;

    /** 参团时间 */
    private LocalDateTime createTime;
}
