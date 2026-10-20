package com.fresh.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 员工登录请求参数
 */
@Data
@Schema(description = "员工登录请求参数")
public class EmployeeLoginDTO implements Serializable {

    /** 登录账号 */
    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /** 密码 */
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
