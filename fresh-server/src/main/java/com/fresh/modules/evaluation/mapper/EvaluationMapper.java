package com.fresh.modules.evaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.modules.evaluation.entity.Evaluation;
import com.fresh.modules.evaluation.vo.EvaluationVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品评价 Mapper
 */
public interface EvaluationMapper extends BaseMapper<Evaluation> {

    /**
     * 商品评价列表（关联用户表带出昵称/头像，按评价时间倒序）
     */
    @Select("SELECT e.*, u.name AS user_name, u.avatar AS avatar " +
            "FROM evaluation e LEFT JOIN user u ON e.user_id = u.id " +
            "WHERE e.product_id = #{productId} ORDER BY e.create_time DESC")
    List<EvaluationVO> listByProductId(@Param("productId") Long productId);
}
