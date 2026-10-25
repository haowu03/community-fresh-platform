package com.fresh.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 发帖入参
 */
@Data
@Schema(description = "发帖入参")
public class PostDTO implements Serializable {

    @Schema(description = "类型 1团购分享 2好物分享")
    private Integer type;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "图片(JSON数组字符串)")
    private String images;
}
