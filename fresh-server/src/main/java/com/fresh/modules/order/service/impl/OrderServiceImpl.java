package com.fresh.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.constant.OrderConstant;
import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.common.constant.StatusConstant;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.common.result.PageResult;
import com.fresh.modules.address.entity.AddressBook;
import com.fresh.modules.address.mapper.AddressBookMapper;
import com.fresh.modules.cart.entity.ShoppingCart;
import com.fresh.modules.cart.mapper.ShoppingCartMapper;
import com.fresh.modules.groupbuy.entity.GroupBuyActivity;
import com.fresh.modules.groupbuy.entity.GroupBuyMember;
import com.fresh.modules.groupbuy.mapper.GroupBuyActivityMapper;
import com.fresh.modules.groupbuy.mapper.GroupBuyMemberMapper;
import com.fresh.modules.notice.service.NoticeService;
import com.fresh.modules.order.dto.*;
import com.fresh.modules.order.entity.OrderDetail;
import com.fresh.modules.order.entity.Orders;
import com.fresh.modules.order.mapper.OrderDetailMapper;
import com.fresh.modules.order.mapper.OrdersMapper;
import com.fresh.modules.order.service.OrderService;
import com.fresh.modules.order.vo.OrderStatisticsVO;
import com.fresh.modules.order.vo.OrderSubmitVO;
import com.fresh.modules.order.vo.OrderVO;
import com.fresh.modules.pickup.entity.PickupPoint;
import com.fresh.modules.pickup.entity.PickupTimeSlot;
import com.fresh.modules.pickup.mapper.PickupPointMapper;
import com.fresh.modules.pickup.mapper.PickupTimeSlotMapper;
import com.fresh.modules.presale.entity.PresaleActivity;
import com.fresh.modules.presale.mapper.PresaleActivityMapper;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.entity.ProductBatch;
import com.fresh.modules.product.mapper.ProductBatchMapper;
import com.fresh.modules.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrderService {

    /** 商品计价方式-按重量 */
    private static final Integer PRICE_TYPE_WEIGHT = 2;
    /** 批次状态-正常 */
    private static final Integer BATCH_STATUS_NORMAL = 1;
    /** 批次状态-临期 */
    private static final Integer BATCH_STATUS_NEAR_EXPIRY = 2;
    /** 预售活动状态-进行中 */
    private static final Integer PRESALE_STATUS_ONGOING = 1;
    /** 团购活动状态-进行中 */
    private static final Integer GROUP_STATUS_ONGOING = 1;
    /** 参团记录状态-已支付 */
    private static final Integer GROUP_MEMBER_PAID = 1;
    /** 通知类型-自提通知 */
    private static final Integer NOTICE_TYPE_PICKUP = 1;
    /** 通知类型-成团通知 */
    private static final Integer NOTICE_TYPE_GROUP_SUCCESS = 2;
    /** 超时未支付分钟数 */
    private static final long PAY_TIMEOUT_MINUTES = 15;

    private final OrdersMapper ordersMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final ProductMapper productMapper;
    private final ProductBatchMapper productBatchMapper;
    private final AddressBookMapper addressBookMapper;
    private final PickupPointMapper pickupPointMapper;
    private final PickupTimeSlotMapper pickupTimeSlotMapper;
    private final PresaleActivityMapper presaleActivityMapper;
    private final GroupBuyActivityMapper groupBuyActivityMapper;
    private final GroupBuyMemberMapper groupBuyMemberMapper;
    private final NoticeService noticeService;
    private final RedisTemplate<String, Object> redisTemplate;

    // =====================================================================
    // 用户端
    // =====================================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submit(OrderSubmitDTO dto) {
        Long userId = BaseContext.getCurrentId();
        // Redis 防重锁：10秒内重复提交直接拒绝
        String lockKey = RedisCacheConstant.ORDER_SUBMIT_LOCK + userId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            throw new BusinessException("请勿重复提交订单");
        }
        try {
            Integer orderType = dto.getOrderType() == null ? OrderConstant.TYPE_NORMAL : dto.getOrderType();
            dto.setOrderType(orderType);

            Orders orders = new Orders();
            orders.setUserId(userId);
            orders.setOrderType(orderType);
            orders.setOrderTime(LocalDateTime.now());
            orders.setStatus(OrderConstant.PENDING_PAYMENT);
            orders.setPayStatus(OrderConstant.PAY_UNPAID);
            orders.setPayMethod(dto.getPayMethod());
            orders.setRemark(dto.getRemark());
            orders.setNumber(generateOrderNumber());

            // 1. 按订单类型构建明细并计算商品总金额
            List<OrderDetail> detailList = new ArrayList<>();
            BigDecimal totalAmount;
            if (OrderConstant.TYPE_PRESALE.equals(orderType)) {
                totalAmount = buildPresaleDetail(dto, orders, detailList);
            } else if (OrderConstant.TYPE_GROUP_BUY.equals(orderType)) {
                totalAmount = buildGroupBuyDetail(dto, userId, orders, detailList);
            } else {
                totalAmount = buildNormalDetail(userId, detailList);
            }

            // 2. 履约方式校验（配送校验地址 / 自提校验自提点并占用时间段）
            Integer deliveryType = dto.getDeliveryType() == null ? OrderConstant.DELIVERY_HOME : dto.getDeliveryType();
            orders.setDeliveryType(deliveryType);
            if (OrderConstant.DELIVERY_HOME.equals(deliveryType)) {
                fillDeliveryAddress(dto, userId, orders);
            } else {
                occupyPickup(dto, orders);
            }

            // 3. 普通单扣减批次库存（临期优先）并同步商品总库存与 Redis 缓存
            if (OrderConstant.TYPE_NORMAL.equals(orderType)) {
                for (OrderDetail detail : detailList) {
                    deductStockWithBatch(detail);
                }
            }

            // 4. 写入订单与明细
            BigDecimal packAmount = dto.getPackAmount() == null ? BigDecimal.ZERO : dto.getPackAmount();
            orders.setPackAmount(packAmount);
            orders.setAmount(totalAmount.add(packAmount).setScale(2, RoundingMode.HALF_UP));
            save(orders);
            for (OrderDetail detail : detailList) {
                detail.setOrderId(orders.getId());
                orderDetailMapper.insert(detail);
            }

            // 5. 普通单下单成功后清空购物车
            if (OrderConstant.TYPE_NORMAL.equals(orderType)) {
                shoppingCartMapper.delete(new LambdaQueryWrapper<ShoppingCart>()
                        .eq(ShoppingCart::getUserId, userId));
            }

            return OrderSubmitVO.builder()
                    .id(orders.getId())
                    .orderNumber(orders.getNumber())
                    .orderAmount(orders.getAmount())
                    .orderTime(orders.getOrderTime())
                    .build();
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payment(OrdersPaymentDTO dto) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = getOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getNumber, dto.getOrderNumber())
                .last("LIMIT 1"));
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        if (!userId.equals(orders.getUserId())) {
            throw new BusinessException("无权支付他人订单");
        }
        if (!OrderConstant.PENDING_PAYMENT.equals(orders.getStatus())) {
            throw new BusinessException("订单状态异常，无法支付");
        }
        // 模拟支付成功：置已支付、待接单
        orders.setPayStatus(OrderConstant.PAY_PAID);
        orders.setStatus(OrderConstant.TO_BE_CONFIRMED);
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setPayMethod(dto.getPayMethod());
        updateById(orders);

        // 预售单：累加活动已售数量
        if (OrderConstant.TYPE_PRESALE.equals(orders.getOrderType())) {
            ordersMapper.increasePresaleSold(orders.getPresaleId(), sumDetailNumber(orders.getId()));
        }

        // 团购单：写参团记录、参团人数+1，达成成团条件则置已成团并通知团长
        if (OrderConstant.TYPE_GROUP_BUY.equals(orders.getOrderType())) {
            GroupBuyMember member = new GroupBuyMember();
            member.setActivityId(orders.getGroupActivityId());
            member.setUserId(userId);
            member.setOrderId(orders.getId());
            member.setStatus(GROUP_MEMBER_PAID);
            member.setCreateTime(LocalDateTime.now());
            try {
                groupBuyMemberMapper.insert(member);
            } catch (DuplicateKeyException e) {
                // 唯一约束 (activity_id, user_id) 兜底防重复参团
                throw new BusinessException("您已参与过该团购");
            }
            ordersMapper.increaseGroupJoined(orders.getGroupActivityId());
            // 达到成团人数则活动置为已成团，返回受影响行数>0说明本次触发成团
            int closed = ordersMapper.closeGroupIfFull(orders.getGroupActivityId());
            if (closed > 0) {
                GroupBuyActivity activity = groupBuyActivityMapper.selectById(orders.getGroupActivityId());
                if (activity != null) {
                    noticeService.send(activity.getLeaderId(), NOTICE_TYPE_GROUP_SUCCESS, "成团通知",
                            "您发起的团购活动（ID：" + activity.getId() + "）已达到成团人数，请安排统一配送至自提点。");
                }
            }
        }
    }

    @Override
    public PageResult historyOrders(OrdersPageQueryDTO dto) {
        Page<Orders> page = new Page<>(dto.getPage(), dto.getPageSize());
        page(page, new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, BaseContext.getCurrentId())
                .eq(dto.getStatus() != null, Orders::getStatus, dto.getStatus())
                .orderByDesc(Orders::getOrderTime));
        List<OrderVO> voList = page.getRecords().stream().map(this::toOrderVO).toList();
        return new PageResult(page.getTotal(), voList);
    }

    @Override
    public OrderVO orderDetail(Long id) {
        Orders orders = getById(id);
        if (orders == null || !BaseContext.getCurrentId().equals(orders.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        return toOrderVO(orders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void userCancel(Long id) {
        Orders orders = getById(id);
        if (orders == null || !BaseContext.getCurrentId().equals(orders.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        // 仅待付款/待接单可取消
        if (!OrderConstant.PENDING_PAYMENT.equals(orders.getStatus())
                && !OrderConstant.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new BusinessException("当前订单状态不可取消");
        }
        orders.setCancelReason("用户主动取消");
        cancelWithRollback(orders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void repetition(Long id) {
        Long userId = BaseContext.getCurrentId();
        Orders orders = getById(id);
        if (orders == null || !userId.equals(orders.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        List<OrderDetail> details = listDetails(id);
        for (OrderDetail detail : details) {
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setProductId(detail.getProductId());
            cart.setName(detail.getName());
            cart.setImage(detail.getImage());
            cart.setNumber(detail.getNumber());
            cart.setWeight(detail.getWeight());
            cart.setAmount(detail.getAmount());
            cart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(cart);
        }
    }

    @Override
    public void reminder(Long id) {
        Orders orders = getById(id);
        if (orders == null || !BaseContext.getCurrentId().equals(orders.getUserId())) {
            throw new BusinessException("订单不存在");
        }
        // 模拟催单：实际项目可推送管理端 websocket/消息
        log.info("用户催单，订单id：{}，订单号：{}", id, orders.getNumber());
    }

    // =====================================================================
    // 管理端
    // =====================================================================

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO dto) {
        Page<Orders> page = new Page<>(dto.getPage(), dto.getPageSize());
        page(page, new LambdaQueryWrapper<Orders>()
                .like(StringUtils.hasText(dto.getNumber()), Orders::getNumber, dto.getNumber())
                .like(StringUtils.hasText(dto.getPhone()), Orders::getPhone, dto.getPhone())
                .eq(dto.getStatus() != null, Orders::getStatus, dto.getStatus())
                .eq(dto.getUserId() != null, Orders::getUserId, dto.getUserId())
                .ge(dto.getBeginTime() != null, Orders::getOrderTime, dto.getBeginTime())
                .le(dto.getEndTime() != null, Orders::getOrderTime, dto.getEndTime())
                .orderByDesc(Orders::getOrderTime));
        List<OrderVO> voList = page.getRecords().stream().map(this::toOrderVO).toList();
        return new PageResult(page.getTotal(), voList);
    }

    @Override
    public OrderVO details(Long id) {
        Orders orders = getById(id);
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        return toOrderVO(orders);
    }

    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = Math.toIntExact(count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, OrderConstant.TO_BE_CONFIRMED)));
        Integer confirmed = Math.toIntExact(count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, OrderConstant.CONFIRMED)));
        Integer deliveryInProgress = Math.toIntExact(count(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, OrderConstant.DELIVERY_IN_PROGRESS)));
        return new OrderStatisticsVO(toBeConfirmed, confirmed, deliveryInProgress);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long id) {
        Orders orders = getById(id);
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        if (!OrderConstant.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new BusinessException("仅待接单订单可接单");
        }
        orders.setStatus(OrderConstant.CONFIRMED);
        updateById(orders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejection(OrdersRejectionDTO dto) {
        Orders orders = getById(dto.getId());
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        if (!OrderConstant.TO_BE_CONFIRMED.equals(orders.getStatus())) {
            throw new BusinessException("仅待接单订单可拒单");
        }
        if (!StringUtils.hasText(dto.getRejectionReason())) {
            throw new BusinessException("拒单原因不能为空");
        }
        // 已支付订单拒单：置退款并回滚库存/时间段
        orders.setRejectionReason(dto.getRejectionReason());
        orders.setCancelReason("商家拒单：" + dto.getRejectionReason());
        cancelWithRollback(orders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delivery(Long id) {
        Orders orders = getById(id);
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        if (!OrderConstant.CONFIRMED.equals(orders.getStatus())) {
            throw new BusinessException("仅备货中的订单可执行该操作");
        }
        if (OrderConstant.DELIVERY_PICKUP.equals(orders.getDeliveryType())) {
            // 自提单：生成6位数字自提码，状态→待自提，发送自提通知
            String pickupCode = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
            orders.setPickupCode(pickupCode);
            orders.setStatus(OrderConstant.DELIVERY_IN_PROGRESS);
            updateById(orders);
            PickupPoint point = orders.getPickupPointId() == null ? null
                    : pickupPointMapper.selectById(orders.getPickupPointId());
            String pointName = point == null ? "自提点" : point.getName();
            noticeService.send(orders.getUserId(), NOTICE_TYPE_PICKUP, "自提通知",
                    "您的订单 " + orders.getNumber() + " 已备货完成，请凭自提码【" + pickupCode + "】到「" + pointName + "」取货。");
        } else {
            // 配送单：状态→配送中
            orders.setStatus(OrderConstant.DELIVERY_IN_PROGRESS);
            updateById(orders);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long id) {
        Orders orders = getById(id);
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        if (!OrderConstant.DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new BusinessException("订单当前状态不可完成");
        }
        orders.setStatus(OrderConstant.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        updateById(orders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminCancel(OrdersCancelDTO dto) {
        Orders orders = getById(dto.getId());
        if (orders == null) {
            throw new BusinessException("订单不存在");
        }
        if (OrderConstant.COMPLETED.equals(orders.getStatus())
                || OrderConstant.CANCELLED.equals(orders.getStatus())) {
            throw new BusinessException("订单已完成或已取消，无法操作");
        }
        orders.setCancelReason(StringUtils.hasText(dto.getCancelReason()) ? dto.getCancelReason() : "商家取消订单");
        cancelWithRollback(orders);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verify(OrderVerifyDTO dto, Long operatorUserId, boolean isAdmin) {
        if (!StringUtils.hasText(dto.getPickupCode())) {
            throw new BusinessException("自提码不能为空");
        }
        Orders orders = getOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getPickupCode, dto.getPickupCode())
                .last("LIMIT 1"));
        if (orders == null) {
            throw new BusinessException("自提码无效");
        }
        if (!OrderConstant.DELIVERY_IN_PROGRESS.equals(orders.getStatus())) {
            throw new BusinessException("订单当前状态不可核销");
        }
        // 团长核销：校验订单自提点属于该团长（pickup_point.leader_id = operatorUserId）
        if (!isAdmin) {
            PickupPoint point = orders.getPickupPointId() == null ? null
                    : pickupPointMapper.selectById(orders.getPickupPointId());
            if (point == null || !operatorUserId.equals(point.getLeaderId())) {
                throw new BusinessException("该订单不属于您管理的自提点，无权核销");
            }
        }
        orders.setStatus(OrderConstant.COMPLETED);
        orders.setPickupTime(LocalDateTime.now());
        orders.setDeliveryTime(LocalDateTime.now());
        updateById(orders);
        // 累计商品销量
        for (OrderDetail detail : listDetails(orders.getId())) {
            ordersMapper.increaseSaleCount(detail.getProductId(), detailQuantity(detail));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(PAY_TIMEOUT_MINUTES);
        List<Orders> timeoutOrders = list(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getStatus, OrderConstant.PENDING_PAYMENT)
                .lt(Orders::getOrderTime, deadline));
        for (Orders orders : timeoutOrders) {
            log.info("超时未支付订单自动取消，订单id：{}，订单号：{}", orders.getId(), orders.getNumber());
            orders.setCancelReason("订单超时未支付，自动取消");
            cancelWithRollback(orders);
        }
    }

    // =====================================================================
    // 私有辅助方法
    // =====================================================================

    /**
     * 普通单：从购物车取明细并按商品实时单价计算金额
     */
    private BigDecimal buildNormalDetail(Long userId, List<OrderDetail> detailList) {
        List<ShoppingCart> cartList = shoppingCartMapper.selectList(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId));
        if (cartList.isEmpty()) {
            throw new BusinessException("购物车为空，无法下单");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (ShoppingCart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || !StatusConstant.ENABLE.equals(product.getStatus())) {
                throw new BusinessException("商品已下架：" + cart.getName());
            }
            // 按份=price*number，按重=price*weight
            BigDecimal itemAmount = calcAmount(product.getPrice(), product.getPriceType(),
                    cart.getNumber(), cart.getWeight());
            OrderDetail detail = new OrderDetail();
            detail.setName(product.getName());
            detail.setImage(product.getImage());
            detail.setProductId(product.getId());
            detail.setNumber(cart.getNumber());
            detail.setWeight(cart.getWeight());
            detail.setAmount(itemAmount);
            detailList.add(detail);
            total = total.add(itemAmount);
        }
        return total;
    }

    /**
     * 预售单：校验活动状态/截止时间/限购库存，单价取预售价
     */
    private BigDecimal buildPresaleDetail(OrderSubmitDTO dto, Orders orders, List<OrderDetail> detailList) {
        PresaleActivity activity = dto.getPresaleId() == null ? null
                : presaleActivityMapper.selectById(dto.getPresaleId());
        if (activity == null) {
            throw new BusinessException("预售活动不存在");
        }
        if (!PRESALE_STATUS_ONGOING.equals(activity.getStatus())) {
            throw new BusinessException("预售活动未在进行中");
        }
        if (LocalDateTime.now().isAfter(activity.getEndTime())) {
            throw new BusinessException("预售已截止下单");
        }
        int number = dto.getNumber() == null ? 1 : dto.getNumber();
        if (activity.getSoldCount() + number > activity.getStockLimit()) {
            throw new BusinessException("预售库存不足");
        }
        Product product = checkProduct(dto.getProductId());
        BigDecimal itemAmount = calcAmount(activity.getPresalePrice(), product.getPriceType(),
                number, dto.getWeight());
        OrderDetail detail = new OrderDetail();
        detail.setName(product.getName());
        detail.setImage(product.getImage());
        detail.setProductId(product.getId());
        detail.setNumber(number);
        detail.setWeight(dto.getWeight());
        detail.setAmount(itemAmount);
        detailList.add(detail);
        orders.setPresaleId(activity.getId());
        return itemAmount;
    }

    /**
     * 团购单：校验活动时间/状态/重复参团，单价取团购价，强制自提且自提点取活动自提点
     */
    private BigDecimal buildGroupBuyDetail(OrderSubmitDTO dto, Long userId, Orders orders,
                                           List<OrderDetail> detailList) {
        GroupBuyActivity activity = dto.getGroupActivityId() == null ? null
                : groupBuyActivityMapper.selectById(dto.getGroupActivityId());
        if (activity == null) {
            throw new BusinessException("团购活动不存在");
        }
        if (!GROUP_STATUS_ONGOING.equals(activity.getStatus())) {
            throw new BusinessException("团购活动未在进行中");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BusinessException("当前不在团购活动时间内");
        }
        // 同一用户同一活动只能参团一次（唯一约束兜底）
        Long joined = groupBuyMemberMapper.selectCount(new LambdaQueryWrapper<GroupBuyMember>()
                .eq(GroupBuyMember::getActivityId, activity.getId())
                .eq(GroupBuyMember::getUserId, userId));
        if (joined > 0) {
            throw new BusinessException("您已参与过该团购");
        }
        Product product = checkProduct(dto.getProductId());
        int number = dto.getNumber() == null ? 1 : dto.getNumber();
        BigDecimal itemAmount = calcAmount(activity.getGroupPrice(), product.getPriceType(),
                number, dto.getWeight());
        OrderDetail detail = new OrderDetail();
        detail.setName(product.getName());
        detail.setImage(product.getImage());
        detail.setProductId(product.getId());
        detail.setNumber(number);
        detail.setWeight(dto.getWeight());
        detail.setAmount(itemAmount);
        detailList.add(detail);
        orders.setGroupActivityId(activity.getId());
        // 团购强制自提，自提点取活动绑定的统一自提点
        dto.setDeliveryType(OrderConstant.DELIVERY_PICKUP);
        dto.setPickupPointId(activity.getPickupPointId());
        return itemAmount;
    }

    /**
     * 配送单：校验并冗余收货地址信息
     */
    private void fillDeliveryAddress(OrderSubmitDTO dto, Long userId, Orders orders) {
        if (dto.getAddressBookId() == null) {
            throw new BusinessException("配送订单必须选择收货地址");
        }
        AddressBook addressBook = addressBookMapper.selectById(dto.getAddressBookId());
        if (addressBook == null || !userId.equals(addressBook.getUserId())) {
            throw new BusinessException("收货地址不存在");
        }
        orders.setAddressBookId(addressBook.getId());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserName(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(buildFullAddress(addressBook));
    }

    /**
     * 自提单：校验自提点/时间段启用，并乐观占用时间段名额
     */
    private void occupyPickup(OrderSubmitDTO dto, Orders orders) {
        if (dto.getPickupPointId() == null || dto.getTimeSlotId() == null) {
            throw new BusinessException("自提订单必须选择自提点和自提时间段");
        }
        PickupPoint point = pickupPointMapper.selectById(dto.getPickupPointId());
        if (point == null || !StatusConstant.ENABLE.equals(point.getStatus())) {
            throw new BusinessException("自提点不存在或已停用");
        }
        PickupTimeSlot slot = pickupTimeSlotMapper.selectById(dto.getTimeSlotId());
        if (slot == null || !StatusConstant.ENABLE.equals(slot.getStatus())) {
            throw new BusinessException("自提时间段不存在或已停用");
        }
        // 乐观占用：used_orders<max_orders 才能 +1，受影响行数=0 说明已满
        int rows = ordersMapper.occupyTimeSlot(dto.getTimeSlotId());
        if (rows == 0) {
            throw new BusinessException("该自提时间段预约已满，请选择其他时间段");
        }
        orders.setPickupPointId(point.getId());
        orders.setTimeSlotId(slot.getId());
        orders.setAddress(point.getDetailAddress());
        orders.setPhone(point.getPhone());
    }

    /**
     * 扣减库存：临期批次优先（到期日升序），不足再扣正常批次；随后同步商品总库存与 Redis 库存缓存
     */
    private void deductStockWithBatch(OrderDetail detail) {
        int need = detailQuantity(detail);
        Long productId = detail.getProductId();
        // 先临期批次(status=2)，再正常批次(status=1)，均按到期日升序
        for (Integer batchStatus : new Integer[]{BATCH_STATUS_NEAR_EXPIRY, BATCH_STATUS_NORMAL}) {
            if (need <= 0) {
                break;
            }
            List<ProductBatch> batches = productBatchMapper.selectList(new LambdaQueryWrapper<ProductBatch>()
                    .eq(ProductBatch::getProductId, productId)
                    .eq(ProductBatch::getStatus, batchStatus)
                    .gt(ProductBatch::getStock, 0)
                    .orderByAsc(ProductBatch::getExpiryDate));
            for (ProductBatch batch : batches) {
                if (need <= 0) {
                    break;
                }
                int deductQty = Math.min(need, batch.getStock());
                // 乐观扣减：并发下受影响行数=0 则尝试下一批次
                int rows = ordersMapper.deductBatchStock(batch.getId(), deductQty);
                if (rows == 0) {
                    continue;
                }
                if (detail.getBatchId() == null) {
                    detail.setBatchId(batch.getId());
                }
                need -= deductQty;
            }
        }
        if (need > 0) {
            throw new BusinessException("商品库存不足：" + detail.getName());
        }
        // 同步商品总库存（乐观扣减）
        int quantity = detailQuantity(detail);
        int rows = ordersMapper.deductProductStock(productId, quantity);
        if (rows == 0) {
            throw new BusinessException("商品库存不足：" + detail.getName());
        }
        // 同步 Redis 库存缓存（缓存存在才更新，避免凭空建负值key）
        String stockKey = RedisCacheConstant.PRODUCT_STOCK_PREFIX + productId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(stockKey))) {
            redisTemplate.opsForValue().decrement(stockKey, quantity);
        }
    }

    /**
     * 取消/拒单通用：已支付置退款、状态置已取消，回滚库存、时间段预约数、预售销量、团购参团
     */
    private void cancelWithRollback(Orders orders) {
        if (OrderConstant.PAY_PAID.equals(orders.getPayStatus())) {
            // 模拟退款
            orders.setPayStatus(OrderConstant.PAY_REFUND);
        }
        orders.setStatus(OrderConstant.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        updateById(orders);

        // 回滚批次/商品库存（仅普通单走过批次扣减）
        if (OrderConstant.TYPE_NORMAL.equals(orders.getOrderType())) {
            for (OrderDetail detail : listDetails(orders.getId())) {
                int quantity = detailQuantity(detail);
                if (detail.getBatchId() != null) {
                    ordersMapper.increaseBatchStock(detail.getBatchId(), quantity);
                }
                ordersMapper.increaseProductStock(detail.getProductId(), quantity);
                String stockKey = RedisCacheConstant.PRODUCT_STOCK_PREFIX + detail.getProductId();
                if (Boolean.TRUE.equals(redisTemplate.hasKey(stockKey))) {
                    redisTemplate.opsForValue().increment(stockKey, quantity);
                }
            }
        }
        // 回滚自提时间段预约数
        if (orders.getTimeSlotId() != null) {
            ordersMapper.releaseTimeSlot(orders.getTimeSlotId());
        }
        // 预售单已支付：回滚预售销量
        if (OrderConstant.TYPE_PRESALE.equals(orders.getOrderType())
                && OrderConstant.PAY_REFUND.equals(orders.getPayStatus())
                && orders.getPresaleId() != null) {
            ordersMapper.decreasePresaleSold(orders.getPresaleId(), sumDetailNumber(orders.getId()));
        }
        // 团购单：删除参团记录并回滚参团人数
        if (OrderConstant.TYPE_GROUP_BUY.equals(orders.getOrderType())
                && orders.getGroupActivityId() != null) {
            int removed = groupBuyMemberMapper.delete(new LambdaQueryWrapper<GroupBuyMember>()
                    .eq(GroupBuyMember::getActivityId, orders.getGroupActivityId())
                    .eq(GroupBuyMember::getUserId, orders.getUserId()));
            if (removed > 0) {
                ordersMapper.decreaseGroupJoined(orders.getGroupActivityId());
            }
        }
    }

    /**
     * 组装订单展示对象：订单字段 + 明细 + 自提点名称 + 时间段描述
     */
    private OrderVO toOrderVO(Orders orders) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(orders, vo);
        vo.setOrderDetailList(listDetails(orders.getId()));
        if (orders.getPickupPointId() != null) {
            PickupPoint point = pickupPointMapper.selectById(orders.getPickupPointId());
            if (point != null) {
                vo.setPickupPointName(point.getName());
            }
        }
        if (orders.getTimeSlotId() != null) {
            PickupTimeSlot slot = pickupTimeSlotMapper.selectById(orders.getTimeSlotId());
            if (slot != null) {
                vo.setTimeSlotDesc(slot.getStartTime() + "~" + slot.getEndTime());
            }
        }
        return vo;
    }

    private List<OrderDetail> listDetails(Long orderId) {
        return orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getOrderId, orderId));
    }

    private int sumDetailNumber(Long orderId) {
        return listDetails(orderId).stream().mapToInt(this::detailQuantity).sum();
    }

    /** 明细扣减/统计数量：份数为空按1计 */
    private int detailQuantity(OrderDetail detail) {
        return detail.getNumber() == null ? 1 : detail.getNumber();
    }

    /** 校验商品存在且在售 */
    private Product checkProduct(Long productId) {
        Product product = productId == null ? null : productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!StatusConstant.ENABLE.equals(product.getStatus())) {
            throw new BusinessException("商品已下架：" + product.getName());
        }
        return product;
    }

    /** 金额计算：按份=price*number，按重=price*weight，保留2位小数 */
    private BigDecimal calcAmount(BigDecimal price, Integer priceType, Integer number, BigDecimal weight) {
        if (PRICE_TYPE_WEIGHT.equals(priceType)) {
            if (weight == null) {
                throw new BusinessException("按重量商品缺少称重重量");
            }
            return price.multiply(weight).setScale(2, RoundingMode.HALF_UP);
        }
        int num = number == null ? 1 : number;
        return price.multiply(BigDecimal.valueOf(num)).setScale(2, RoundingMode.HALF_UP);
    }

    private String buildFullAddress(AddressBook addressBook) {
        StringBuilder sb = new StringBuilder();
        if (addressBook.getProvinceName() != null) {
            sb.append(addressBook.getProvinceName());
        }
        if (addressBook.getCityName() != null) {
            sb.append(addressBook.getCityName());
        }
        if (addressBook.getDistrictName() != null) {
            sb.append(addressBook.getDistrictName());
        }
        sb.append(addressBook.getDetail());
        return sb.toString();
    }

    /** 生成订单号：时间戳 + 4位随机数 */
    private String generateOrderNumber() {
        return String.valueOf(System.currentTimeMillis())
                + (1000 + ThreadLocalRandom.current().nextInt(9000));
    }
}
