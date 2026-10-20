package com.fresh.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工分页查询请求参数
 */
@Data
@Schema(description = "员工分页查询请求参数")
public class EmployeePageQueryDTO implements Serializable {

    /** 页码 */
    @Schema(description = "页码", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer page;

    /** 每页条数 */
    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer pageSize;

    /** 员工姓名（模糊查询） */
    @Schema(description = "员工姓名（模糊查询）")
    private String name;
}
