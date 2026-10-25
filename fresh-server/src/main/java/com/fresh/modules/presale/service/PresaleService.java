package com.fresh.modules.presale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.common.result.PageResult;
import com.fresh.modules.presale.dto.PresaleActivityDTO;
import com.fresh.modules.presale.dto.PresalePageQueryDTO;
import com.fresh.modules.presale.entity.PresaleActivity;
import com.fresh.modules.presale.vo.PresaleActivityVO;

import java.util.List;

/**
 * 预售活动 Service
 */
public interface PresaleService extends IService<PresaleActivity> {

    /**
     * 管理端分页查询
     */
    PageResult pageQuery(PresalePageQueryDTO dto);

    /**
     * 发布预售活动：校验商品与时间，商品 is_presale 置1，写Redis缓存
     */
    void publish(PresaleActivityDTO dto);

    /**
     * 修改预售活动
     */
    void update(PresaleActivityDTO dto);

    /**
     * 手动提前结束
     */
    void close(Long id);

    /**
     * 删除预售活动
     */
    void deleteById(Long id);

    /**
     * 用户端：进行中活动列表
     */
    List<PresaleActivityVO> listOngoing();

    /**
     * 用户端：活动详情（活动+商品信息，优先读Redis）
     */
    PresaleActivityVO detail(Long id);
}
