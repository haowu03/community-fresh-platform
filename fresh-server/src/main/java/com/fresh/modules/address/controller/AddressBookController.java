package com.fresh.modules.address.controller;

import com.fresh.common.result.R;
import com.fresh.modules.address.entity.AddressBook;
import com.fresh.modules.address.service.AddressBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端-地址簿接口
 */
@Slf4j
@RestController
@RequestMapping("/user/addressBook")
@RequiredArgsConstructor
@Tag(name = "地址模块", description = "用户端-地址簿管理接口")
public class AddressBookController {

    private final AddressBookService addressBookService;

    /**
     * 新增地址
     */
    @PostMapping
    @Operation(summary = "新增地址")
    public R<Void> save(@RequestBody AddressBook addressBook) {
        addressBookService.saveAddress(addressBook);
        return R.success();
    }

    /**
     * 查询当前用户全部地址
     */
    @GetMapping("/list")
    @Operation(summary = "地址列表")
    public R<List<AddressBook>> list() {
        return R.success(addressBookService.listByUser());
    }

    /**
     * 查询当前用户默认地址
     */
    @GetMapping("/default")
    @Operation(summary = "默认地址")
    public R<AddressBook> getDefault() {
        return R.success(addressBookService.getDefault());
    }

    /**
     * 修改地址
     */
    @PutMapping
    @Operation(summary = "修改地址")
    public R<Void> update(@RequestBody AddressBook addressBook) {
        addressBookService.updateAddress(addressBook);
        return R.success();
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default")
    @Operation(summary = "设为默认地址")
    public R<Void> setDefault(@RequestBody AddressBook addressBook) {
        addressBookService.setDefault(addressBook.getId());
        return R.success();
    }

    /**
     * 删除地址
     */
    @DeleteMapping
    @Operation(summary = "删除地址")
    public R<Void> delete(Long id) {
        addressBookService.deleteById(id);
        return R.success();
    }
}
