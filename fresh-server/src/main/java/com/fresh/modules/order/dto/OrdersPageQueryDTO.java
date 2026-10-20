package com.fresh.modules.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单分页查询入参（用户端历史订单 / 管理端条件搜索通用）
 */
@Data
@Schema(description = "订单分页查询入参")
public class OrdersPageQueryDTO implements Serializable {

    /** 页码 */
    @Schema(description = "页码")
    private Integer page = 1;

    /** 每页条数 */
    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    /** 订单号 */
    @Schema(description = "订单号")
    private String number;

    /** 手机号 */
    @Schema(description = "手机号")
    private String phone;

    /** 订单状态 */
    @Schema(description = "订单状态 1待付款 2待接单 3备货中 4配送中/待自提 5已完成 6已取消")
    private Integer status;

    /** 下单时间起 */
    @Schema(description = "下单时间起")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    /** 下单时间止 */
    @Schema(description = "下单时间止")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 用户id(管理端按用户筛选) */
    @Schema(description = "用户id")
    private Long userId;
}
