package com.fresh.modules.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类分页查询入参
 */
@Data
@Schema(description = "分类分页查询入参")
public class CategoryPageQueryDTO implements Serializable {

    @Schema(description = "页码", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer page;

    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pageSize;

    @Schema(description = "分类名称(模糊)")
    private String name;
}
