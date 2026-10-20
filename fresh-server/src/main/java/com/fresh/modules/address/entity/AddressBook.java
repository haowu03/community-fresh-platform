package com.fresh.modules.address.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 地址簿实体，对应 address_book 表
 */
@Data
@TableName("address_book")
public class AddressBook implements Serializable {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户id */
    private Long userId;

    /** 收货人 */
    private String consignee;

    /** 性别 */
    private String sex;

    /** 手机号 */
    private String phone;

    /** 省级区划编号 */
    private String provinceCode;

    /** 省级名称 */
    private String provinceName;

    /** 市级区划编号 */
    private String cityCode;

    /** 市级名称 */
    private String cityName;

    /** 区级区划编号 */
    private String districtCode;

    /** 区级名称 */
    private String districtName;

    /** 详细地址 */
    private String detail;

    /** 标签: 家/公司/学校 */
    private String label;

    /** 是否默认 1是 0否 */
    private Integer isDefault;
}
