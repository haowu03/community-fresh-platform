package com.fresh.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 帖子分页查询入参（用户端）
 */
@Data
@Schema(description = "帖子分页查询入参")
public class PostPageQueryDTO implements Serializable {

    @Schema(description = "类型 1团购分享 2好物分享")
    private Integer type;

    @Schema(description = "页码")
    private Integer page = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;
}
