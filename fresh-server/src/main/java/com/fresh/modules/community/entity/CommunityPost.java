package com.fresh.modules.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社区帖子实体
 */
@Data
@TableName("community_post")
public class CommunityPost implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布用户id */
    private Long userId;

    /** 类型 1团购分享 2好物分享 */
    private Integer type;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 图片(JSON数组字符串) */
    private String images;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /** 审核状态 0待审核 1已通过 2已拒绝 */
    private Integer status;

    /** 审核意见 */
    private String auditRemark;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核人(employee.id) */
    private Long auditorId;

    /** 发布时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;
}
