package com.fresh.modules.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论展示VO
 */
@Data
@Schema(description = "评论展示VO")
public class CommentVO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "帖子id")
    private Long postId;

    @Schema(description = "评论用户id")
    private Long userId;

    @Schema(description = "评论用户昵称")
    private String userName;

    @Schema(description = "评论用户头像")
    private String avatar;

    @Schema(description = "父评论id")
    private Long parentId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论时间")
    private LocalDateTime createTime;

    @Schema(description = "子评论列表")
    private List<CommentVO> children;
}
