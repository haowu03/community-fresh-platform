package com.fresh.modules.groupbuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fresh.common.constant.OrderConstant;
import com.fresh.common.constant.RoleConstant;
import com.fresh.common.constant.StatusConstant;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.modules.groupbuy.dto.GroupBuyLaunchDTO;
import com.fresh.modules.groupbuy.entity.GroupBuyActivity;
import com.fresh.modules.groupbuy.entity.GroupBuyMember;
import com.fresh.modules.groupbuy.mapper.GroupBuyActivityMapper;
import com.fresh.modules.groupbuy.mapper.GroupBuyMemberMapper;
import com.fresh.modules.notice.service.NoticeService;
import com.fresh.modules.order.entity.Orders;
import com.fresh.modules.order.mapper.OrdersMapper;
import com.fresh.modules.pickup.entity.PickupPoint;
import com.fresh.modules.pickup.mapper.PickupPointMapper;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.mapper.ProductMapper;
import com.fresh.modules.user.entity.User;
import com.fresh.modules.user.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 社区团购核心逻辑单元测试：发起校验、成团结算、流团退款
 */
@ExtendWith(MockitoExtension.class)
class GroupBuyServiceImplTest {

    @Mock
    private GroupBuyActivityMapper groupBuyActivityMapper;
    @Mock
    private GroupBuyMemberMapper groupBuyMemberMapper;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PickupPointMapper pickupPointMapper;
    @Mock
    private OrdersMapper ordersMapper;
    @Mock
    private NoticeService noticeService;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private GroupBuyServiceImpl groupBuyService;

    @BeforeEach
    void setUp() {
        BaseContext.setCurrentId(1L);
        // Mockito 不注入父类 ServiceImpl 的 baseMapper，手动注入 mock
        ReflectionTestUtils.setField(groupBuyService, "baseMapper", groupBuyActivityMapper);
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    // ==================== 结算：成团 ====================

    @Test
    @DisplayName("达到成团人数：活动置已成团并通知团长")
    void settleOne_grouped() {
        GroupBuyActivity activity = new GroupBuyActivity();
        activity.setId(100L);
        activity.setProductId(10L);
        activity.setLeaderId(1L);
        activity.setJoinedNum(3);
        activity.setMinNum(3);
        activity.setStatus(1);

        Product product = new Product();
        product.setId(10L);
        product.setName("苹果");
        when(productMapper.selectById(10L)).thenReturn(product);

        List<GroupBuyActivity> updated = new ArrayList<>();
        doAnswer(invocation -> {
            updated.add(invocation.getArgument(0));
            return 1;
        }).when(groupBuyActivityMapper).updateById(any(GroupBuyActivity.class));

        groupBuyService.settleOne(activity);

        assertEquals(2, updated.get(0).getStatus(), "达到成团人数应置为已成团(2)");
        verify(noticeService).send(eq(1L), eq(2), eq("团购已成团"), any());
        // 成团场景不应退款任何成员
        verify(groupBuyMemberMapper, never()).updateById(any(GroupBuyMember.class));
    }

    // ==================== 结算：流团退款 ====================

    @Test
    @DisplayName("未达人数流团：订单取消退款、成员置已退款并逐个通知")
    void settleOne_failed() {
        GroupBuyActivity activity = new GroupBuyActivity();
        activity.setId(100L);
        activity.setProductId(10L);
        activity.setLeaderId(1L);
        activity.setJoinedNum(1);
        activity.setMinNum(3);
        activity.setStatus(1);

        when(productMapper.selectById(10L)).thenReturn(new Product());

        GroupBuyMember member = new GroupBuyMember();
        member.setId(1L);
        member.setActivityId(100L);
        member.setUserId(2L);
        member.setOrderId(200L);
        member.setStatus(1);
        when(groupBuyMemberMapper.selectList(any(Wrapper.class))).thenReturn(List.of(member));

        Orders order = new Orders();
        order.setId(200L);
        order.setStatus(OrderConstant.PENDING_PAYMENT);
        order.setPayStatus(OrderConstant.PAY_PAID);
        when(ordersMapper.selectById(200L)).thenReturn(order);

        List<GroupBuyActivity> activityUpdated = new ArrayList<>();
        doAnswer(invocation -> {
            activityUpdated.add(invocation.getArgument(0));
            return 1;
        }).when(groupBuyActivityMapper).updateById(any(GroupBuyActivity.class));

        List<Orders> orderUpdated = new ArrayList<>();
        doAnswer(invocation -> {
            orderUpdated.add(invocation.getArgument(0));
            return 1;
        }).when(ordersMapper).updateById(any(Orders.class));

        List<GroupBuyMember> memberUpdated = new ArrayList<>();
        doAnswer(invocation -> {
            memberUpdated.add(invocation.getArgument(0));
            return 1;
        }).when(groupBuyMemberMapper).updateById(any(GroupBuyMember.class));

        groupBuyService.settleOne(activity);

        assertEquals(4, activityUpdated.get(0).getStatus(), "未达标应置为已流团(4)");
        assertEquals(OrderConstant.CANCELLED, orderUpdated.get(0).getStatus());
        assertEquals(OrderConstant.PAY_REFUND, orderUpdated.get(0).getPayStatus());
        assertEquals(2, memberUpdated.get(0).getStatus(), "成员应置为已退款(2)");

        verify(noticeService).send(eq(2L), eq(3), eq("团购流团退款通知"), any());
    }

    // ==================== 发起校验 ====================

    @Test
    @DisplayName("非团长角色发起团购：拒绝")
    void launch_notLeader() {
        User normal = new User();
        normal.setId(1L);
        normal.setRole(1);
        when(userMapper.selectById(anyLong())).thenReturn(normal);

        BusinessException e = assertThrows(BusinessException.class,
                () -> groupBuyService.launch(new GroupBuyLaunchDTO()));
        assertEquals("仅团长可发起团购", e.getMessage());
    }

    @Test
    @DisplayName("自提点不属于当前团长：拒绝")
    void launch_pickupPointNotOwned() {
        User leader = new User();
        leader.setId(1L);
        leader.setRole(RoleConstant.LEADER);
        when(userMapper.selectById(1L)).thenReturn(leader);

        PickupPoint point = new PickupPoint();
        point.setId(5L);
        point.setLeaderId(999L);
        when(pickupPointMapper.selectById(5L)).thenReturn(point);

        GroupBuyLaunchDTO dto = new GroupBuyLaunchDTO();
        dto.setPickupPointId(5L);

        BusinessException e = assertThrows(BusinessException.class, () -> groupBuyService.launch(dto));
        assertEquals("该自提点不属于当前团长", e.getMessage());
    }

    @Test
    @DisplayName("团购价高于原价：拒绝")
    void launch_priceTooHigh() {
        User leader = new User();
        leader.setId(1L);
        leader.setRole(RoleConstant.LEADER);
        when(userMapper.selectById(1L)).thenReturn(leader);

        PickupPoint point = new PickupPoint();
        point.setId(5L);
        point.setLeaderId(1L);
        when(pickupPointMapper.selectById(5L)).thenReturn(point);

        Product product = new Product();
        product.setId(10L);
        product.setStatus(StatusConstant.ENABLE);
        product.setPrice(new BigDecimal("9.90"));
        when(productMapper.selectById(10L)).thenReturn(product);

        GroupBuyLaunchDTO dto = new GroupBuyLaunchDTO();
        dto.setPickupPointId(5L);
        dto.setProductId(10L);
        dto.setGroupPrice(new BigDecimal("12.00"));
        dto.setMinNum(2);
        dto.setStartTime(LocalDateTime.now().minusHours(1));
        dto.setEndTime(LocalDateTime.now().plusDays(1));

        BusinessException e = assertThrows(BusinessException.class, () -> groupBuyService.launch(dto));
        assertEquals("团购价不能高于商品原价", e.getMessage());
    }

    @Test
    @DisplayName("成团人数少于2人：拒绝")
    void launch_minNumInvalid() {
        stubLeaderAndPointAndProduct(new BigDecimal("8.00"));

        GroupBuyLaunchDTO dto = validDto();
        dto.setMinNum(1);

        BusinessException e = assertThrows(BusinessException.class, () -> groupBuyService.launch(dto));
        assertEquals("成团人数至少为2人", e.getMessage());
    }

    @Test
    @DisplayName("发起团购成功：已过开始时间则直接进行中")
    void launch_success() {
        stubLeaderAndPointAndProduct(new BigDecimal("8.00"));

        List<GroupBuyActivity> saved = new ArrayList<>();
        doAnswer(invocation -> {
            saved.add(invocation.getArgument(0));
            return 1;
        }).when(groupBuyActivityMapper).insert(any(GroupBuyActivity.class));

        groupBuyService.launch(validDto());

        GroupBuyActivity created = saved.get(0);
        assertEquals(1, created.getStatus(), "开始时间已过应直接置为进行中");
        assertEquals(0, created.getJoinedNum());
        assertEquals(1L, created.getLeaderId());
    }

    // ==================== 私有辅助 ====================

    private void stubLeaderAndPointAndProduct(BigDecimal price) {
        User leader = new User();
        leader.setId(1L);
        leader.setRole(RoleConstant.LEADER);
        when(userMapper.selectById(1L)).thenReturn(leader);

        PickupPoint point = new PickupPoint();
        point.setId(5L);
        point.setLeaderId(1L);
        when(pickupPointMapper.selectById(5L)).thenReturn(point);

        Product product = new Product();
        product.setId(10L);
        product.setStatus(StatusConstant.ENABLE);
        product.setPrice(price);
        when(productMapper.selectById(10L)).thenReturn(product);
    }

    private GroupBuyLaunchDTO validDto() {
        GroupBuyLaunchDTO dto = new GroupBuyLaunchDTO();
        dto.setPickupPointId(5L);
        dto.setProductId(10L);
        dto.setGroupPrice(new BigDecimal("8.00"));
        dto.setMinNum(2);
        dto.setStartTime(LocalDateTime.now().minusHours(1));
        dto.setEndTime(LocalDateTime.now().plusDays(1));
        return dto;
    }
}