package com.fresh.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 评论入参
 */
@Data
@Schema(description = "评论入参")
public class CommentDTO implements Serializable {

    @Schema(description = "帖子id")
    private Long postId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "父评论id(楼中楼)")
    private Long parentId;
}
