package com.fresh.modules.community.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子展示VO
 */
@Data
@Schema(description = "帖子展示VO")
public class PostVO implements Serializable {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "发布用户id")
    private Long userId;

    @Schema(description = "发布用户昵称")
    private String userName;

    @Schema(description = "发布用户头像")
    private String avatar;

    @Schema(description = "类型 1团购分享 2好物分享")
    private Integer type;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "图片(JSON数组字符串)")
    private String images;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "审核状态 0待审核 1已通过 2已拒绝")
    private Integer status;

    @Schema(description = "审核意见")
    private String auditRemark;

    @Schema(description = "当前用户是否已赞")
    private Boolean liked;

    @Schema(description = "发布时间")
    private LocalDateTime createTime;
}
