package com.fresh.modules.evaluation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fresh.modules.evaluation.entity.Evaluation;
import com.fresh.modules.evaluation.vo.EvaluationVO;
import com.fresh.modules.order.dto.EvaluationDTO;

import java.util.List;

/**
 * 商品评价服务
 */
public interface EvaluationService extends IService<Evaluation> {

    /**
     * 提交评价：订单须属当前用户且已完成，同一订单同一商品只能评价一次
     */
    void submit(EvaluationDTO dto);

    /**
     * 商品评价列表（带用户昵称/头像）
     */
    List<EvaluationVO> listByProduct(Long productId);
}
