package com.fresh.modules.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子点赞实体（post_id + user_id 唯一约束）
 */
@Data
@TableName("post_like")
public class PostLike implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 帖子id */
    private Long postId;

    /** 点赞用户id */
    private Long userId;

    /** 点赞时间 */
    private LocalDateTime createTime;
}
