package com.fresh.modules.groupbuy.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 团购活动分页查询入参
 */
@Data
@Schema(description = "团购活动分页查询入参")
public class GroupBuyPageQueryDTO implements Serializable {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "状态 0未开始 1进行中 2已成团 3已完成 4已流团")
    private Integer status;
}
