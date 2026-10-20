package com.fresh.modules.notice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知实体，对应 notice 表（自提/成团/系统通知）
 */
@Data
@TableName("notice")
public class Notice implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收用户id */
    private Long userId;

    /** 类型 1自提通知 2成团通知 3流团退款通知 4系统通知 */
    private Integer type;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 是否已读 1是 0否 */
    private Integer isRead;

    /** 创建时间 */
    private LocalDateTime createTime;
}
