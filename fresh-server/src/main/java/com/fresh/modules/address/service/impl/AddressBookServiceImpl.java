package com.fresh.modules.address.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.modules.address.entity.AddressBook;
import com.fresh.modules.address.mapper.AddressBookMapper;
import com.fresh.modules.address.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 地址簿 Service 实现类
 */
@Slf4j
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    /** 是否默认：是 */
    private static final Integer IS_DEFAULT = 1;
    /** 是否默认：否 */
    private static final Integer NOT_DEFAULT = 0;

    /**
     * 新增地址：归属当前登录用户；若是该用户第一条地址则自动设为默认
     */
    @Override
    public void saveAddress(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setId(null);
        addressBook.setUserId(userId);
        // 该用户首条地址自动设为默认，否则按传入值处理
        long count = count(new LambdaQueryWrapper<AddressBook>().eq(AddressBook::getUserId, userId));
        if (count == 0) {
            addressBook.setIsDefault(IS_DEFAULT);
        } else if (addressBook.getIsDefault() == null) {
            addressBook.setIsDefault(NOT_DEFAULT);
        }
        save(addressBook);
    }

    /**
     * 查询当前登录用户的全部地址
     */
    @Override
    public List<AddressBook> listByUser() {
        Long userId = BaseContext.getCurrentId();
        return list(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId)
                .orderByDesc(AddressBook::getIsDefault)
                .orderByDesc(AddressBook::getId));
    }

    /**
     * 查询当前登录用户的默认地址
     */
    @Override
    public AddressBook getDefault() {
        Long userId = BaseContext.getCurrentId();
        return getOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getIsDefault, IS_DEFAULT));
    }

    /**
     * 修改地址：校验地址归属当前用户，防止越权修改
     */
    @Override
    public void updateAddress(AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        AddressBook dbAddress = getById(addressBook.getId());
        if (dbAddress == null || !dbAddress.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        // userId 不允许被修改
        addressBook.setUserId(userId);
        updateById(addressBook);
    }

    /**
     * 设置默认地址：先将该用户其他地址置为非默认，再设置目标地址
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        Long userId = BaseContext.getCurrentId();
        AddressBook dbAddress = getById(id);
        if (dbAddress == null || !dbAddress.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        // 1. 将该用户所有地址置为非默认
        update(new LambdaUpdateWrapper<AddressBook>()
                .eq(AddressBook::getUserId, userId)
                .set(AddressBook::getIsDefault, NOT_DEFAULT));
        // 2. 将目标地址置为默认
        update(new LambdaUpdateWrapper<AddressBook>()
                .eq(AddressBook::getId, id)
                .set(AddressBook::getIsDefault, IS_DEFAULT));
    }

    /**
     * 删除地址：仅允许删除本人地址
     */
    @Override
    public void deleteById(Long id) {
        Long userId = BaseContext.getCurrentId();
        AddressBook dbAddress = getById(id);
        if (dbAddress == null || !dbAddress.getUserId().equals(userId)) {
            throw new BusinessException("地址不存在");
        }
        removeById(id);
    }
}
