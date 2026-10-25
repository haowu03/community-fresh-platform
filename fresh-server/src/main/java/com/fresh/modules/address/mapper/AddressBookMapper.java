package com.fresh.modules.address.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.modules.address.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

/**
 * 地址簿 Mapper
 */
@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
