package com.fresh.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改个人信息请求参数
 */
@Data
@Schema(description = "修改个人信息请求参数")
public class UserInfoDTO implements Serializable {

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
}
