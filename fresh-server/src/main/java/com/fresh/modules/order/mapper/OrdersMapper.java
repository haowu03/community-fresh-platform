package com.fresh.modules.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fresh.modules.order.entity.Orders;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 订单 Mapper，含并发安全的数据库乐观更新语句
 */
public interface OrdersMapper extends BaseMapper<Orders> {

    /**
     * 乐观占用自提时间段：预约数+1，超出容量则受影响行数为0
     */
    @Update("UPDATE pickup_time_slot SET used_orders = used_orders + 1 " +
            "WHERE id = #{id} AND used_orders < max_orders")
    int occupyTimeSlot(@Param("id") Long id);

    /**
     * 释放自提时间段预约数（取消订单时）
     */
    @Update("UPDATE pickup_time_slot SET used_orders = used_orders - 1 " +
            "WHERE id = #{id} AND used_orders > 0")
    int releaseTimeSlot(@Param("id") Long id);

    /**
     * 乐观扣减批次库存：库存充足才扣减，否则受影响行数为0
     */
    @Update("UPDATE product_batch SET stock = stock - #{quantity} " +
            "WHERE id = #{id} AND stock >= #{quantity}")
    int deductBatchStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 回滚批次库存（取消/拒单时）
     */
    @Update("UPDATE product_batch SET stock = stock + #{quantity} WHERE id = #{id}")
    int increaseBatchStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 乐观扣减商品总库存
     */
    @Update("UPDATE product SET stock = stock - #{quantity} " +
            "WHERE id = #{id} AND stock >= #{quantity}")
    int deductProductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 回滚商品总库存
     */
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{id}")
    int increaseProductStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 累计商品销量（核销/完成时）
     */
    @Update("UPDATE product SET sale_count = sale_count + #{count} WHERE id = #{id}")
    int increaseSaleCount(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 累加预售活动已售数量
     */
    @Update("UPDATE presale_activity SET sold_count = sold_count + #{count} WHERE id = #{id}")
    int increasePresaleSold(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 回滚预售活动已售数量
     */
    @Update("UPDATE presale_activity SET sold_count = GREATEST(sold_count - #{count}, 0) WHERE id = #{id}")
    int decreasePresaleSold(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 团购参团人数+1
     */
    @Update("UPDATE group_buy_activity SET joined_num = joined_num + 1 WHERE id = #{id}")
    int increaseGroupJoined(@Param("id") Long id);

    /**
     * 团购参团人数-1
     */
    @Update("UPDATE group_buy_activity SET joined_num = GREATEST(joined_num - 1, 0) WHERE id = #{id}")
    int decreaseGroupJoined(@Param("id") Long id);

    /**
     * 达到成团人数则将活动置为已成团(status=2)，返回受影响行数用于判断是否触发成团通知
     */
    @Update("UPDATE group_buy_activity SET status = 2 " +
            "WHERE id = #{id} AND status = 1 AND joined_num >= min_num")
    int closeGroupIfFull(@Param("id") Long id);
}
