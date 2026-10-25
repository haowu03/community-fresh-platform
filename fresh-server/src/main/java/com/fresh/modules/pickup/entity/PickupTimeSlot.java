package com.fresh.modules.pickup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * 自提时间段实体
 */
@Data
@TableName("pickup_time_slot")
public class PickupTimeSlot implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 自提点id */
    private Long pointId;

    /** 开始时间 */
    private LocalTime startTime;

    /** 结束时间 */
    private LocalTime endTime;

    /** 该时段最大订单数 */
    private Integer maxOrders;

    /** 已预约订单数 */
    private Integer usedOrders;

    /** 状态 1启用 0停用 */
    private Integer status;
}
