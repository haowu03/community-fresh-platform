package com.fresh.modules.pickup.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.constant.OrderConstant;
import com.fresh.common.constant.StatusConstant;
import com.fresh.common.exception.BusinessException;
import com.fresh.common.result.PageResult;
import com.fresh.modules.order.entity.Orders;
import com.fresh.modules.order.mapper.OrdersMapper;
import com.fresh.modules.pickup.dto.PickupPointDTO;
import com.fresh.modules.pickup.dto.PickupPointPageQueryDTO;
import com.fresh.modules.pickup.dto.TimeSlotDTO;
import com.fresh.modules.pickup.entity.PickupPoint;
import com.fresh.modules.pickup.entity.PickupTimeSlot;
import com.fresh.modules.pickup.mapper.PickupPointMapper;
import com.fresh.modules.pickup.mapper.PickupTimeSlotMapper;
import com.fresh.modules.pickup.service.PickupPointService;
import com.fresh.modules.pickup.vo.PickupPointVO;
import com.fresh.modules.user.entity.User;
import com.fresh.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自提点 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PickupPointServiceImpl extends ServiceImpl<PickupPointMapper, PickupPoint>
        implements PickupPointService {

    private final PickupTimeSlotMapper pickupTimeSlotMapper;
    private final UserMapper userMapper;
    private final OrdersMapper ordersMapper;

    @Override
    public PageResult pageQuery(PickupPointPageQueryDTO dto) {
        Page<PickupPoint> page = new Page<>(dto.getPage(), dto.getPageSize());
        LambdaQueryWrapper<PickupPoint> wrapper = new LambdaQueryWrapper<PickupPoint>()
                .like(StrUtil.isNotBlank(dto.getName()), PickupPoint::getName, dto.getName())
                .eq(dto.getStatus() != null, PickupPoint::getStatus, dto.getStatus())
                .orderByDesc(PickupPoint::getCreateTime);
        this.page(page, wrapper);

        List<PickupPointVO> voList = page.getRecords().stream().map(point -> {
            PickupPointVO vo = BeanUtil.copyProperties(point, PickupPointVO.class);
            vo.setLeaderName(getLeaderName(point.getLeaderId()));
            return vo;
        }).collect(Collectors.toList());
        return new PageResult(page.getTotal(), voList);
    }

    @Override
    public void add(PickupPointDTO dto) {
        PickupPoint point = BeanUtil.copyProperties(dto, PickupPoint.class);
        if (point.getStatus() == null) {
            point.setStatus(StatusConstant.ENABLE);
        }
        point.setCreateTime(LocalDateTime.now());
        point.setUpdateTime(LocalDateTime.now());
        this.save(point);
    }

    @Override
    public void update(PickupPointDTO dto) {
        if (dto.getId() == null || this.getById(dto.getId()) == null) {
            throw new BusinessException("自提点不存在");
        }
        PickupPoint point = BeanUtil.copyProperties(dto, PickupPoint.class);
        point.setUpdateTime(LocalDateTime.now());
        this.updateById(point);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        // 校验：存在进行中订单（待接单/备货中/配送中待自提）的自提点禁止删除
        Long count = ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getPickupPointId, id)
                .in(Orders::getStatus, OrderConstant.TO_BE_CONFIRMED,
                        OrderConstant.CONFIRMED, OrderConstant.DELIVERY_IN_PROGRESS));
        if (count != null && count > 0) {
            throw new BusinessException("该自提点存在进行中的订单，禁止删除");
        }
        this.removeById(id);
        // 级联删除该自提点下的时间段
        pickupTimeSlotMapper.delete(new LambdaQueryWrapper<PickupTimeSlot>()
                .eq(PickupTimeSlot::getPointId, id));
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        PickupPoint point = this.getById(id);
        if (point == null) {
            throw new BusinessException("自提点不存在");
        }
        point.setStatus(status);
        point.setUpdateTime(LocalDateTime.now());
        this.updateById(point);
    }

    @Override
    public void saveOrUpdateSlot(TimeSlotDTO dto) {
        if (dto.getPointId() == null || this.getById(dto.getPointId()) == null) {
            throw new BusinessException("自提点不存在");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null
                || !dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BusinessException("时间段不合法");
        }
        if (dto.getMaxOrders() == null || dto.getMaxOrders() <= 0) {
            throw new BusinessException("最大订单数必须大于0");
        }
        PickupTimeSlot slot = BeanUtil.copyProperties(dto, PickupTimeSlot.class);
        if (slot.getId() == null) {
            // 新增：已预约数从0开始
            slot.setUsedOrders(0);
            if (slot.getStatus() == null) {
                slot.setStatus(StatusConstant.ENABLE);
            }
            pickupTimeSlotMapper.insert(slot);
        } else {
            // 修改：不覆盖已预约数
            PickupTimeSlot db = pickupTimeSlotMapper.selectById(slot.getId());
            if (db == null) {
                throw new BusinessException("时间段不存在");
            }
            slot.setUsedOrders(null);
            pickupTimeSlotMapper.updateById(slot);
        }
    }

    @Override
    public void deleteSlot(Long id) {
        pickupTimeSlotMapper.deleteById(id);
    }

    @Override
    public List<PickupTimeSlot> listSlots(Long pointId) {
        return pickupTimeSlotMapper.selectList(new LambdaQueryWrapper<PickupTimeSlot>()
                .eq(PickupTimeSlot::getPointId, pointId)
                .orderByAsc(PickupTimeSlot::getStartTime));
    }

    @Override
    public List<PickupPointVO> listEnabled() {
        List<PickupPoint> points = this.list(new LambdaQueryWrapper<PickupPoint>()
                .eq(PickupPoint::getStatus, StatusConstant.ENABLE)
                .orderByDesc(PickupPoint::getCreateTime));
        if (CollUtil.isEmpty(points)) {
            return Collections.emptyList();
        }
        return points.stream().map(point -> {
            PickupPointVO vo = BeanUtil.copyProperties(point, PickupPointVO.class);
            vo.setLeaderName(getLeaderName(point.getLeaderId()));
            vo.setTimeSlots(listSlots(point.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<PickupTimeSlot> listAvailableSlots(Long pointId) {
        // 启用且未约满：used_orders < max_orders
        return pickupTimeSlotMapper.selectList(new LambdaQueryWrapper<PickupTimeSlot>()
                .eq(PickupTimeSlot::getPointId, pointId)
                .eq(PickupTimeSlot::getStatus, StatusConstant.ENABLE)
                .apply("used_orders < max_orders")
                .orderByAsc(PickupTimeSlot::getStartTime));
    }

    /**
     * 查询团长昵称
     */
    private String getLeaderName(Long leaderId) {
        if (leaderId == null) {
            return null;
        }
        User leader = userMapper.selectById(leaderId);
        return leader == null ? null : leader.getName();
    }

}
