package com.fresh.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增/修改员工请求参数
 */
@Data
@Schema(description = "新增/修改员工请求参数")
public class EmployeeDTO implements Serializable {

    /** 员工id（修改时必传） */
    @Schema(description = "员工id（修改时必传）")
    private Long id;

    /** 登录账号 */
    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    /** 姓名 */
    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /** 手机号 */
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;

    /** 性别 */
    @Schema(description = "性别")
    private String sex;

    /** 身份证号 */
    @Schema(description = "身份证号")
    private String idNumber;
}
