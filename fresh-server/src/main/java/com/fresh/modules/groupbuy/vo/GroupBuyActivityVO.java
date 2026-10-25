package com.fresh.modules.groupbuy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 团购活动展示VO（含进度）
 */
@Data
@Schema(description = "团购活动展示VO")
public class GroupBuyActivityVO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "团购商品id")
    private Long productId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "商品原价")
    private BigDecimal originalPrice;

    @Schema(description = "发起团长id")
    private Long leaderId;

    @Schema(description = "团长昵称")
    private String leaderName;

    @Schema(description = "自提点id")
    private Long pickupPointId;

    @Schema(description = "自提点名称")
    private String pointName;

    @Schema(description = "团购价")
    private BigDecimal groupPrice;

    @Schema(description = "成团人数")
    private Integer minNum;

    @Schema(description = "已参团人数")
    private Integer joinedNum;

    @Schema(description = "剩余名额 = minNum - joinedNum")
    private Integer remainNum;

    @Schema(description = "进度百分比(0-100)")
    private Integer progress;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "截止时间")
    private LocalDateTime endTime;

    @Schema(description = "状态 0未开始 1进行中 2已成团 3已完成 4已流团")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
