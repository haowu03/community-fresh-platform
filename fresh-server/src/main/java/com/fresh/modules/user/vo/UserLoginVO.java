package com.fresh.modules.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户登录返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录返回结果")
public class UserLoginVO implements Serializable {

    /** 用户id */
    @Schema(description = "用户id")
    private Long id;

    /** 登录账号 */
    @Schema(description = "登录账号")
    private String username;

    /** 昵称 */
    @Schema(description = "昵称")
    private String name;

    /** 角色 1普通用户 2团长 */
    @Schema(description = "角色 1普通用户 2团长")
    private Integer role;

    /** JWT令牌 */
    @Schema(description = "JWT令牌")
    private String token;
}
