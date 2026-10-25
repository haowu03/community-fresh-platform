package com.fresh.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改密码请求参数
 */
@Data
@Schema(description = "修改密码请求参数")
public class PasswordEditDTO implements Serializable {

    /** 旧密码 */
    @Schema(description = "旧密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oldPassword;

    /** 新密码 */
    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
