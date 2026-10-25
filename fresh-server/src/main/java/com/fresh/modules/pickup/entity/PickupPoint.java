package com.fresh.modules.pickup.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 社区自提点实体
 */
@Data
@TableName("pickup_point")
public class PickupPoint implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 自提点名称 */
    private String name;

    /** 绑定团长(user.id) */
    private Long leaderId;

    /** 省 */
    private String provinceName;

    /** 市 */
    private String cityName;

    /** 区 */
    private String districtName;

    /** 详细地址 */
    private String detailAddress;

    /** 经度 */
    private BigDecimal lng;

    /** 纬度 */
    private BigDecimal lat;

    /** 联系电话 */
    private String phone;

    /** 营业时间描述 */
    private String businessHours;

    /** 状态 1启用 0停用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 修改时间 */
    private LocalDateTime updateTime;
}
