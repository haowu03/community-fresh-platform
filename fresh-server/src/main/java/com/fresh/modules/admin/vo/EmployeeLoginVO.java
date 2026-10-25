package com.fresh.modules.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 员工登录返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "员工登录返回结果")
public class EmployeeLoginVO implements Serializable {

    /** 员工id */
    @Schema(description = "员工id")
    private Long id;

    /** 姓名 */
    @Schema(description = "姓名")
    private String name;

    /** JWT令牌 */
    @Schema(description = "JWT令牌")
    private String token;
}
