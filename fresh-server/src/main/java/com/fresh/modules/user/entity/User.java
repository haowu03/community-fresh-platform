package com.fresh.modules.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * C端用户实体（含团长角色），对应 user 表
 */
@Data
@TableName("user")
public class User implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号 */
    private String username;

    /** 密码(BCrypt加密) */
    private String password;

    /** 昵称 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 性别 0女 1男 */
    private String sex;

    /** 头像 */
    private String avatar;

    /** 角色 1普通用户 2团长 */
    private Integer role;

    /** 状态 1正常 0禁用 */
    private Integer status;

    /** 注册时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;
}
