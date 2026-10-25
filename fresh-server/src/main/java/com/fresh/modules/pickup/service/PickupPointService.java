package com.fresh.modules.pickup.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.common.result.PageResult;
import com.fresh.modules.pickup.dto.PickupPointDTO;
import com.fresh.modules.pickup.dto.PickupPointPageQueryDTO;
import com.fresh.modules.pickup.dto.TimeSlotDTO;
import com.fresh.modules.pickup.entity.PickupPoint;
import com.fresh.modules.pickup.entity.PickupTimeSlot;
import com.fresh.modules.pickup.vo.PickupPointVO;

import java.util.List;

/**
 * 自提点 Service
 */
public interface PickupPointService extends IService<PickupPoint> {

    /**
     * 管理端分页查询（含团长昵称）
     */
    PageResult pageQuery(PickupPointPageQueryDTO dto);

    /**
     * 新增自提点
     */
    void add(PickupPointDTO dto);

    /**
     * 修改自提点
     */
    void update(PickupPointDTO dto);

    /**
     * 删除自提点（有进行中订单的自提点禁止删除）
     */
    void deleteById(Long id);

    /**
     * 启用/停用自提点
     */
    void updateStatus(Long id, Integer status);

    /**
     * 新增/修改时间段（id为空新增）
     */
    void saveOrUpdateSlot(TimeSlotDTO dto);

    /**
     * 删除时间段
     */
    void deleteSlot(Long id);

    /**
     * 查询自提点全部时间段
     */
    List<PickupTimeSlot> listSlots(Long pointId);

    /**
     * 用户端：启用自提点列表（含时间段）
     */
    List<PickupPointVO> listEnabled();

    /**
     * 用户端：该点启用且未约满的时间段
     */
    List<PickupTimeSlot> listAvailableSlots(Long pointId);
}
