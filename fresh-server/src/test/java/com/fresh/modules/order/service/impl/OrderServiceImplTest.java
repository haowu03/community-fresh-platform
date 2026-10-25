package com.fresh.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fresh.common.constant.OrderConstant;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.modules.address.mapper.AddressBookMapper;
import com.fresh.modules.cart.entity.ShoppingCart;
import com.fresh.modules.cart.mapper.ShoppingCartMapper;
import com.fresh.modules.groupbuy.entity.GroupBuyActivity;
import com.fresh.modules.groupbuy.entity.GroupBuyMember;
import com.fresh.modules.groupbuy.mapper.GroupBuyActivityMapper;
import com.fresh.modules.groupbuy.mapper.GroupBuyMemberMapper;
import com.fresh.modules.notice.service.NoticeService;
import com.fresh.modules.order.dto.OrderSubmitDTO;
import com.fresh.modules.order.dto.OrderVerifyDTO;
import com.fresh.modules.order.dto.OrdersPaymentDTO;
import com.fresh.modules.order.entity.OrderDetail;
import com.fresh.modules.order.entity.Orders;
import com.fresh.modules.order.mapper.OrderDetailMapper;
import com.fresh.modules.order.mapper.OrdersMapper;
import com.fresh.modules.pickup.mapper.PickupPointMapper;
import com.fresh.modules.pickup.mapper.PickupTimeSlotMapper;
import com.fresh.modules.presale.mapper.PresaleActivityMapper;
import com.fresh.modules.product.mapper.ProductBatchMapper;
import com.fresh.modules.product.mapper.ProductMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 订单服务核心链路单元测试：防重锁、团购支付成团、自提码核销、超时关单
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrdersMapper ordersMapper;
    @Mock
    private OrderDetailMapper orderDetailMapper;
    @Mock
    private ShoppingCartMapper shoppingCartMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ProductBatchMapper productBatchMapper;
    @Mock
    private AddressBookMapper addressBookMapper;
    @Mock
    private PickupPointMapper pickupPointMapper;
    @Mock
    private PickupTimeSlotMapper pickupTimeSlotMapper;
    @Mock
    private PresaleActivityMapper presaleActivityMapper;
    @Mock
    private GroupBuyActivityMapper groupBuyActivityMapper;
    @Mock
    private GroupBuyMemberMapper groupBuyMemberMapper;
    @Mock
    private NoticeService noticeService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(1L);
        // Mockito 不注入父类 ServiceImpl 的 baseMapper，手动注入 mock
        ReflectionTestUtils.setField(orderService, "baseMapper", ordersMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    // ==================== 下单防重锁 ====================

    @Test
    @DisplayName("10 秒内重复提交订单：Redis 防重锁拒绝")
    void submit_duplicateSubmissionRejected() {
        when(valueOperations.setIfAbsent(any(), any(), anyLong(), any())).thenReturn(false);

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.submit(new OrderSubmitDTO()));
        assertEquals("请勿重复提交订单", e.getMessage());
    }

    // ==================== 支付：团购成团 ====================

    @Test
    @DisplayName("团购单支付且达到成团人数：触发成团并通知团长")
    void payment_groupBuyPaymentTriggersGroupClose() {
        Orders order = groupBuyOrder();
        when(ordersMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(order);
        when(ordersMapper.closeGroupIfFull(100L)).thenReturn(1);

        GroupBuyActivity activity = new GroupBuyActivity();
        activity.setId(100L);
        activity.setLeaderId(5L);
        when(groupBuyActivityMapper.selectById(100L)).thenReturn(activity);

        List<Orders> orderUpdated = new ArrayList<>();
        doAnswer(invocation -> {
            orderUpdated.add(invocation.getArgument(0));
            return 1;
        }).when(ordersMapper).updateById(any(Orders.class));

        List<GroupBuyMember> savedMember = new ArrayList<>();
        doAnswer(invocation -> {
            savedMember.add(invocation.getArgument(0));
            return 1;
        }).when(groupBuyMemberMapper).insert(any(GroupBuyMember.class));

        orderService.payment(new OrdersPaymentDTO());

        // 订单状态：待付款 -> 待接单
        assertEquals(OrderConstant.TO_BE_CONFIRMED, orderUpdated.get(0).getStatus());
        assertEquals(OrderConstant.PAY_PAID, orderUpdated.get(0).getPayStatus());

        // 参团记录已支付
        assertEquals(100L, savedMember.get(0).getActivityId());
        assertEquals(1, savedMember.get(0).getStatus());

        // 参团人数 +1，达到成团条件并通知团长
        verify(ordersMapper).increaseGroupJoined(100L);
        verify(noticeService).send(eq(5L), eq(2), eq("成团通知"), any());
    }

    @Test
    @DisplayName("重复参团：唯一约束兜底拒绝")
    void payment_duplicateJoinRejected() {
        Orders order = groupBuyOrder();
        when(ordersMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(order);
        doThrow(new DuplicateKeyException("dup")).when(groupBuyMemberMapper).insert(any(GroupBuyMember.class));

        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.payment(new OrdersPaymentDTO()));
        assertEquals("您已参与过该团购", e.getMessage());
    }

    // ==================== 自提码核销 ====================

    @Test
    @DisplayName("自提码无效：拒绝核销")
    void verify_invalidCode() {
        when(ordersMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);

        OrderVerifyDTO dto = new OrderVerifyDTO();
        dto.setPickupCode("000000");
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.verify(dto, null, true));
        assertEquals("自提码无效", e.getMessage());
    }

    @Test
    @DisplayName("订单状态不可核销：拒绝")
    void verify_wrongStatus() {
        Orders order = new Orders();
        order.setId(1L);
        order.setPickupCode("000000");
        order.setStatus(OrderConstant.COMPLETED);
        when(ordersMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(order);

        OrderVerifyDTO dto = new OrderVerifyDTO();
        dto.setPickupCode("000000");
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.verify(dto, null, true));
        assertEquals("订单当前状态不可核销", e.getMessage());
    }

    @Test
    @DisplayName("团长核销他人自提点的订单：无权核销")
    void verify_leaderNotOwner() {
        Orders order = new Orders();
        order.setId(1L);
        order.setPickupCode("000000");
        order.setStatus(OrderConstant.DELIVERY_IN_PROGRESS);
        order.setPickupPointId(9L);
        when(ordersMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(order);
        when(pickupPointMapper.selectById(9L)).thenReturn(null);

        OrderVerifyDTO dto = new OrderVerifyDTO();
        dto.setPickupCode("000000");
        BusinessException e = assertThrows(BusinessException.class,
                () -> orderService.verify(dto, 7L, false));
        assertEquals("该订单不属于您管理的自提点，无权核销", e.getMessage());
    }

    @Test
    @DisplayName("管理端核销成功：订单完成并累计商品销量")
    void verify_success() {
        Orders order = new Orders();
        order.setId(1L);
        order.setPickupCode("000000");
        order.setStatus(OrderConstant.DELIVERY_IN_PROGRESS);
        when(ordersMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(order);

        OrderDetail detail = new OrderDetail();
        detail.setProductId(7L);
        detail.setNumber(2);
        when(orderDetailMapper.selectList(any(Wrapper.class))).thenReturn(List.of(detail));

        List<Orders> orderUpdated = new ArrayList<>();
        doAnswer(invocation -> {
            orderUpdated.add(invocation.getArgument(0));
            return 1;
        }).when(ordersMapper).updateById(any(Orders.class));

        OrderVerifyDTO dto = new OrderVerifyDTO();
        dto.setPickupCode("000000");
        orderService.verify(dto, null, true);

        assertEquals(OrderConstant.COMPLETED, orderUpdated.get(0).getStatus(), "核销后订单应完成");
        assertNotNull(orderUpdated.get(0).getPickupTime());

        verify(ordersMapper).increaseSaleCount(7L, 2);
    }

    // ==================== 超时关单 ====================

    @Test
    @DisplayName("超时未支付订单：自动取消")
    void cancelTimeoutOrders_cancels() {
        Orders timeout = new Orders();
        timeout.setId(1L);
        timeout.setNumber("N20260101");
        timeout.setStatus(OrderConstant.PENDING_PAYMENT);
        timeout.setPayStatus(OrderConstant.PAY_UNPAID);
        timeout.setOrderType(OrderConstant.TYPE_NORMAL);
        when(ordersMapper.selectList(any(Wrapper.class))).thenReturn(List.of(timeout));
        when(orderDetailMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        List<Orders> orderUpdated = new ArrayList<>();
        doAnswer(invocation -> {
            orderUpdated.add(invocation.getArgument(0));
            return 1;
        }).when(ordersMapper).updateById(any(Orders.class));

        orderService.cancelTimeoutOrders();

        assertEquals(OrderConstant.CANCELLED, orderUpdated.get(0).getStatus());
        assertEquals("订单超时未支付，自动取消", orderUpdated.get(0).getCancelReason());
        verify(shoppingCartMapper, never()).insert(any(ShoppingCart.class));
    }

    // ==================== 私有辅助 ====================

    private Orders groupBuyOrder() {
        Orders order = new Orders();
        order.setId(10L);
        order.setUserId(1L);
        order.setNumber("N100");
        order.setStatus(OrderConstant.PENDING_PAYMENT);
        order.setPayStatus(OrderConstant.PAY_UNPAID);
        order.setOrderType(OrderConstant.TYPE_GROUP_BUY);
        order.setGroupActivityId(100L);
        return order;
    }
}