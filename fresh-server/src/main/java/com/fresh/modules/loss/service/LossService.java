package com.fresh.modules.loss.service;

import com.fresh.common.result.PageResult;

/**
 * 损耗/临期批次管理 Service
 */
public interface LossService {

    /**
     * 批次分页查询（管理端查看临期/过期批次，含商品名）
     *
     * @param status   批次状态 1正常 2临期 3过期（可空查全部）
     * @param page     页码
     * @param pageSize 每页条数
     */
    PageResult batchPage(Integer status, Integer page, Integer pageSize);
}
