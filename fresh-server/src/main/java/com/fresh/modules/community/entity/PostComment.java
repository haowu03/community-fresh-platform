package com.fresh.modules.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子评论实体
 */
@Data
@TableName("post_comment")
public class PostComment implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子id */
    private Long postId;

    /** 评论用户id */
    private Long userId;

    /** 父评论id(楼中楼) */
    private Long parentId;

    /** 评论内容 */
    private String content;

    /** 状态 1正常 0删除 */
    private Integer status;

    /** 评论时间 */
    private LocalDateTime createTime;
}
