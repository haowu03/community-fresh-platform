package com.fresh.modules.address.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.modules.address.entity.AddressBook;

import java.util.List;

/**
 * 地址簿 Service
 */
public interface AddressBookService extends IService<AddressBook> {

    /**
     * 新增地址（归属当前登录用户）
     *
     * @param addressBook 地址信息
     */
    void saveAddress(AddressBook addressBook);

    /**
     * 查询当前登录用户的全部地址
     *
     * @return 地址列表
     */
    List<AddressBook> listByUser();

    /**
     * 查询当前登录用户的默认地址
     *
     * @return 默认地址，无则返回 null
     */
    AddressBook getDefault();

    /**
     * 修改地址（仅允许修改本人地址）
     *
     * @param addressBook 地址信息
     */
    void updateAddress(AddressBook addressBook);

    /**
     * 设置默认地址：先将该用户其他地址置为非默认
     *
     * @param id 地址id
     */
    void setDefault(Long id);

    /**
     * 删除地址（仅允许删除本人地址）
     *
     * @param id 地址id
     */
    void deleteById(Long id);
}
