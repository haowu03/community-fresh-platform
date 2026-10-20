package com.fresh.modules.pickup.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 自提点分页查询入参
 */
@Data
@Schema(description = "自提点分页查询入参")
public class PickupPointPageQueryDTO implements Serializable {

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;

    @Schema(description = "自提点名称(模糊)")
    private String name;

    @Schema(description = "状态 1启用 0停用")
    private Integer status;
}
