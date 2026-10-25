package com.fresh.modules.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.constant.OrderConstant;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.modules.evaluation.entity.Evaluation;
import com.fresh.modules.evaluation.mapper.EvaluationMapper;
import com.fresh.modules.evaluation.service.EvaluationService;
import com.fresh.modules.evaluation.vo.EvaluationVO;
import com.fresh.modules.order.dto.EvaluationDTO;
import com.fresh.modules.order.entity.OrderDetail;
import com.fresh.modules.order.entity.Orders;
import com.fresh.modules.order.mapper.OrderDetailMapper;
import com.fresh.modules.order.mapper.OrdersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品评价服务实现
 */
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl extends ServiceImpl<EvaluationMapper, Evaluation> implements EvaluationService {

    private final OrdersMapper ordersMapper;
    private final OrderDetailMapper orderDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(EvaluationDTO dto) {
        Long userId = BaseContext.getCurrentId();
        // 校验订单属当前用户
        Orders orders = ordersMapper.selectById(dto.getOrderId());
        if (orders == null || !userId.equals(orders.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        if (!OrderConstant.COMPLETED.equals(orders.getStatus())) {
            throw new BusinessException("订单未完成，暂不能评价");
        }
        // 校验商品在该订单中
        Long detailCount = orderDetailMapper.selectCount(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, dto.getOrderId())
                .eq(OrderDetail::getProductId, dto.getProductId()));
        if (detailCount == 0) {
            throw new BusinessException("该商品不在此订单中");
        }
        // 同一订单同一商品只能评价一次
        Long existCount = count(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getOrderId, dto.getOrderId())
                .eq(Evaluation::getProductId, dto.getProductId()));
        if (existCount > 0) {
            throw new BusinessException("该商品已评价过");
        }
        if (dto.getScore() == null || dto.getScore() < 1 || dto.getScore() > 5) {
            throw new BusinessException("评分须在1-5之间");
        }
        Evaluation evaluation = new Evaluation();
        evaluation.setOrderId(dto.getOrderId());
        evaluation.setUserId(userId);
        evaluation.setProductId(dto.getProductId());
        evaluation.setScore(dto.getScore());
        evaluation.setContent(dto.getContent());
        evaluation.setImages(dto.getImages());
        evaluation.setCreateTime(LocalDateTime.now());
        save(evaluation);
    }

    @Override
    public List<EvaluationVO> listByProduct(Long productId) {
        return baseMapper.listByProductId(productId);
    }
}
