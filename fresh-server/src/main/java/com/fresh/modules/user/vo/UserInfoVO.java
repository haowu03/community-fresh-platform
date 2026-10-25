package com.fresh.modules.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息返回结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息返回结果")
public class UserInfoVO implements Serializable {

    /** 用户id */
    @Schema(description = "用户id")
    private Long id;

    /** 登录账号 */
    @Schema(description = "登录账号")
    private String username;

    /** 昵称 */
    @Schema(description = "昵称")
    private String name;

    /** 手机号 */
    @Schema(description = "手机号")
    private String phone;

    /** 性别 0女 1男 */
    @Schema(description = "性别 0女 1男")
    private String sex;

    /** 头像 */
    @Schema(description = "头像")
    private String avatar;

    /** 角色 1普通用户 2团长 */
    @Schema(description = "角色 1普通用户 2团长")
    private Integer role;

    /** 注册时间 */
    @Schema(description = "注册时间")
    private LocalDateTime createTime;
}
