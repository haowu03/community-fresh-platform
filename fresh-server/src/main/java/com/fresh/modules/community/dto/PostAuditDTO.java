package com.fresh.modules.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 帖子审核入参
 */
@Data
@Schema(description = "帖子审核入参")
public class PostAuditDTO implements Serializable {

    @Schema(description = "帖子id")
    private Long id;

    @Schema(description = "审核结果 1通过 2拒绝")
    private Integer status;

    @Schema(description = "审核意见")
    private String auditRemark;
}
