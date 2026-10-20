package com.fresh.modules.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类新增/修改入参
 */
@Data
@Schema(description = "分类新增/修改入参")
public class CategoryDTO implements Serializable {

    @Schema(description = "主键(修改时必传)")
    private Long id;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "排序(升序)")
    private Integer sort;
}
