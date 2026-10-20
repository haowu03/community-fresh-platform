package com.fresh.common.constant;

/**
 * 订单相关常量：状态、订单类型、履约方式、支付状态
 */
public class OrderConstant {

    // ================= 订单状态 =================
    /** 待付款 */
    public static final Integer PENDING_PAYMENT = 1;
    /** 待接单 */
    public static final Integer TO_BE_CONFIRMED = 2;
    /** 备货中/待配送（已接单） */
    public static final Integer CONFIRMED = 3;
    /** 配送中/待自提 */
    public static final Integer DELIVERY_IN_PROGRESS = 4;
    /** 已完成 */
    public static final Integer COMPLETED = 5;
    /** 已取消 */
    public static final Integer CANCELLED = 6;

    // ================= 订单类型 =================
    /** 普通订单 */
    public static final Integer TYPE_NORMAL = 1;
    /** 预售订单 */
    public static final Integer TYPE_PRESALE = 2;
    /** 团购订单 */
    public static final Integer TYPE_GROUP_BUY = 3;

    // ================= 履约方式 =================
    /** 配送到家 */
    public static final Integer DELIVERY_HOME = 1;
    /** 社区自提点自提 */
    public static final Integer DELIVERY_PICKUP = 2;

    // ================= 支付状态 =================
    /** 未支付 */
    public static final Integer PAY_UNPAID = 0;
    /** 已支付 */
    public static final Integer PAY_PAID = 1;
    /** 已退款 */
    public static final Integer PAY_REFUND = 2;
}
