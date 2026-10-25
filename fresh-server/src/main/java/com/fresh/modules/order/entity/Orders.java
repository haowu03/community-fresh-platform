package com.fresh.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体（扩展履约方式/订单类型/自提字段）
 */
@Data
@TableName("orders")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String number;

    /** 状态 1待付款 2待接单 3备货中/待配送 4配送中/待自提 5已完成 6已取消 */
    private Integer status;

    /** 下单用户id */
    private Long userId;

    /** 订单类型 1普通 2预售 3团购 */
    private Integer orderType;

    /** 履约方式 1配送到家 2自提 */
    private Integer deliveryType;

    /** 地址id(配送单) */
    private Long addressBookId;

    /** 自提点id(自提单) */
    private Long pickupPointId;

    /** 自提时间段id */
    private Long timeSlotId;

    /** 自提码(6位数字) */
    private String pickupCode;

    /** 核销时间 */
    private LocalDateTime pickupTime;

    /** 预售活动id */
    private Long presaleId;

    /** 团购活动id */
    private Long groupActivityId;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 支付时间 */
    private LocalDateTime checkoutTime;

    /** 支付方式 1微信 2支付宝 */
    private Integer payMethod;

    /** 支付状态 0未支付 1已支付 2已退款 */
    private Integer payStatus;

    /** 实付金额 */
    private BigDecimal amount;

    /** 打包费 */
    private BigDecimal packAmount;

    /** 备注 */
    private String remark;

    /** 下单人昵称(冗余) */
    private String userName;

    /** 联系手机号 */
    private String phone;

    /** 收货地址/自提点地址(冗余) */
    private String address;

    /** 收货人 */
    private String consignee;

    /** 取消原因 */
    private String cancelReason;

    /** 拒单原因 */
    private String rejectionReason;

    /** 取消时间 */
    private LocalDateTime cancelTime;

    /** 预计送达/可自提时间 */
    private LocalDateTime estimatedDeliveryTime;

    /** 送达/完成时间 */
    private LocalDateTime deliveryTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;
}
