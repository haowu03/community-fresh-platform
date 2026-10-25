package com.fresh.modules.groupbuy.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.constant.OrderConstant;
import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.common.constant.RoleConstant;
import com.fresh.common.constant.StatusConstant;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.common.result.PageResult;
import com.fresh.modules.groupbuy.dto.GroupBuyLaunchDTO;
import com.fresh.modules.groupbuy.dto.GroupBuyPageQueryDTO;
import com.fresh.modules.groupbuy.entity.GroupBuyActivity;
import com.fresh.modules.groupbuy.entity.GroupBuyMember;
import com.fresh.modules.groupbuy.mapper.GroupBuyActivityMapper;
import com.fresh.modules.groupbuy.mapper.GroupBuyMemberMapper;
import com.fresh.modules.groupbuy.service.GroupBuyService;
import com.fresh.modules.groupbuy.vo.GroupBuyActivityVO;
import com.fresh.modules.groupbuy.vo.GroupBuyMemberVO;
import com.fresh.modules.notice.service.NoticeService;
import com.fresh.modules.order.entity.Orders;
import com.fresh.modules.order.mapper.OrdersMapper;
import com.fresh.modules.pickup.entity.PickupPoint;
import com.fresh.modules.pickup.mapper.PickupPointMapper;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.mapper.ProductMapper;
import com.fresh.modules.user.entity.User;
import com.fresh.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 社区团购 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupBuyServiceImpl extends ServiceImpl<GroupBuyActivityMapper, GroupBuyActivity>
        implements GroupBuyService {

    /** 活动状态：未开始 */
    private static final Integer STATUS_NOT_START = 0;
    /** 活动状态：进行中 */
    private static final Integer STATUS_ONGOING = 1;
    /** 活动状态：已成团 */
    private static final Integer STATUS_GROUPED = 2;
    /** 活动状态：已流团 */
    private static final Integer STATUS_FAILED = 4;

    /** 参团成员状态：已支付 */
    private static final Integer MEMBER_PAID = 1;
    /** 参团成员状态：已退款 */
    private static final Integer MEMBER_REFUND = 2;

    /** 通知类型：成团通知 */
    private static final Integer NOTICE_GROUPED = 2;
    /** 通知类型：流团退款通知 */
    private static final Integer NOTICE_REFUND = 3;

    private final GroupBuyMemberMapper groupBuyMemberMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final PickupPointMapper pickupPointMapper;
    private final OrdersMapper ordersMapper;
    private final NoticeService noticeService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult pageQuery(GroupBuyPageQueryDTO dto) {
        Page<GroupBuyActivity> page = new Page<>(dto.getPage(), dto.getPageSize());
        this.page(page, new LambdaQueryWrapper<GroupBuyActivity>()
                .eq(dto.getStatus() != null, GroupBuyActivity::getStatus, dto.getStatus())
                .orderByDesc(GroupBuyActivity::getCreateTime));
        List<GroupBuyActivityVO> voList = page.getRecords().stream()
                .map(this::buildVO)
                .collect(Collectors.toList());
        return new PageResult(page.getTotal(), voList);
    }

    @Override
    public List<GroupBuyActivityVO> listOngoing() {
        List<GroupBuyActivity> list = this.list(new LambdaQueryWrapper<GroupBuyActivity>()
                .eq(GroupBuyActivity::getStatus, STATUS_ONGOING)
                .gt(GroupBuyActivity::getEndTime, LocalDateTime.now())
                .orderByAsc(GroupBuyActivity::getEndTime));
        return list.stream().map(this::buildVO).collect(Collectors.toList());
    }

    @Override
    public GroupBuyActivityVO detail(Long id) {
        // 酌情缓存：优先读Redis中的活动实体
        Object cached = redisTemplate.opsForValue().get(RedisCacheConstant.GROUP_BUY_PREFIX + id);
        GroupBuyActivity activity;
        if (cached instanceof GroupBuyActivity ga) {
            activity = ga;
        } else {
            activity = this.getById(id);
            if (activity == null) {
                throw new BusinessException("团购活动不存在");
            }
            redisTemplate.opsForValue().set(RedisCacheConstant.GROUP_BUY_PREFIX + id, activity);
        }
        return buildVO(activity);
    }

    @Override
    public List<GroupBuyActivityVO> myJoin() {
        Long currentUserId = BaseContext.getCurrentId();
        List<GroupBuyMember> members = groupBuyMemberMapper.selectList(
                new LambdaQueryWrapper<GroupBuyMember>()
                        .eq(GroupBuyMember::getUserId, currentUserId)
                        .orderByDesc(GroupBuyMember::getCreateTime));
        if (CollUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        List<Long> activityIds = members.stream()
                .map(GroupBuyMember::getActivityId).distinct().collect(Collectors.toList());
        List<GroupBuyActivity> activities = this.listByIds(activityIds);
        return activities.stream().map(this::buildVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void launch(GroupBuyLaunchDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();

        // 校验1：仅团长可发起（role=2）
        User user = userMapper.selectById(currentUserId);
        if (user == null || !RoleConstant.LEADER.equals(user.getRole())) {
            throw new BusinessException("仅团长可发起团购");
        }

        // 校验2：自提点须属于该团长
        PickupPoint point = pickupPointMapper.selectById(dto.getPickupPointId());
        if (point == null) {
            throw new BusinessException("自提点不存在");
        }
        if (!currentUserId.equals(point.getLeaderId())) {
            throw new BusinessException("该自提点不属于当前团长");
        }

        // 校验3：商品存在且起售
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!StatusConstant.ENABLE.equals(product.getStatus())) {
            throw new BusinessException("商品未起售，无法发起团购");
        }

        // 校验4：价格/时间/人数合法
        if (dto.getGroupPrice() == null || dto.getGroupPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("团购价必须大于0");
        }
        if (dto.getGroupPrice().compareTo(product.getPrice()) > 0) {
            throw new BusinessException("团购价不能高于商品原价");
        }
        if (dto.getMinNum() == null || dto.getMinNum() < 2) {
            throw new BusinessException("成团人数至少为2人");
        }
        if (dto.getStartTime() == null || dto.getEndTime() == null
                || !dto.getEndTime().isAfter(dto.getStartTime())
                || !dto.getEndTime().isAfter(LocalDateTime.now())) {
            throw new BusinessException("团购时间不合法");
        }

        GroupBuyActivity activity = BeanUtil.copyProperties(dto, GroupBuyActivity.class);
        activity.setLeaderId(currentUserId);
        activity.setJoinedNum(0);
        // 开始时间在未来则未开始，否则直接进行中
        activity.setStatus(dto.getStartTime().isAfter(LocalDateTime.now())
                ? STATUS_NOT_START : STATUS_ONGOING);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        this.save(activity);
        log.info("团长 {} 发起团购成功: activityId={}, productId={}",
                currentUserId, activity.getId(), activity.getProductId());
    }

    @Override
    public List<GroupBuyActivityVO> myLaunch() {
        Long currentUserId = BaseContext.getCurrentId();
        List<GroupBuyActivity> list = this.list(new LambdaQueryWrapper<GroupBuyActivity>()
                .eq(GroupBuyActivity::getLeaderId, currentUserId)
                .orderByDesc(GroupBuyActivity::getCreateTime));
        return list.stream().map(this::buildVO).collect(Collectors.toList());
    }

    @Override
    public List<GroupBuyMemberVO> listMembers(Long id) {
        List<GroupBuyMember> members = groupBuyMemberMapper.selectList(
                new LambdaQueryWrapper<GroupBuyMember>()
                        .eq(GroupBuyMember::getActivityId, id)
                        .orderByAsc(GroupBuyMember::getCreateTime));
        if (CollUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        // 批量查用户与订单，避免N+1
        List<Long> userIds = members.stream()
                .map(GroupBuyMember::getUserId).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        List<Long> orderIds = members.stream()
                .map(GroupBuyMember::getOrderId).distinct().collect(Collectors.toList());
        Map<Long, Orders> orderMap = ordersMapper.selectBatchIds(orderIds).stream()
                .collect(Collectors.toMap(Orders::getId, o -> o));

        return members.stream().map(m -> {
            GroupBuyMemberVO vo = BeanUtil.copyProperties(m, GroupBuyMemberVO.class);
            User u = userMap.get(m.getUserId());
            if (u != null) {
                vo.setUserName(u.getName());
                vo.setPhone(u.getPhone());
            }
            Orders order = orderMap.get(m.getOrderId());
            if (order != null) {
                vo.setOrderAmount(order.getAmount());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void close(Long id) {
        GroupBuyActivity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException("团购活动不存在");
        }
        if (!STATUS_ONGOING.equals(activity.getStatus())) {
            throw new BusinessException("仅进行中的活动可截团");
        }
        doSettle(activity);
        log.info("管理端手动截团完成: activityId={}, status={}", id, activity.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleOne(GroupBuyActivity activity) {
        doSettle(activity);
    }

    /**
     * 结算核心逻辑：达标 → 已成团并通知团长；未达标 → 流团退款并逐个通知
     */
    private void doSettle(GroupBuyActivity activity) {
        activity.setUpdateTime(LocalDateTime.now());
        Product product = productMapper.selectById(activity.getProductId());
        String productName = product == null ? "" : product.getName();

        if (activity.getJoinedNum() >= activity.getMinNum()) {
            // a) 已成团：该活动的待接单订单进入正常履约（支付时已置待接单，无需额外处理）
            activity.setStatus(STATUS_GROUPED);
            this.updateById(activity);
            noticeService.send(activity.getLeaderId(), NOTICE_GROUPED, "团购已成团",
                    "您发起的【" + productName + "】团购已达到成团人数（"
                            + activity.getJoinedNum() + "/" + activity.getMinNum() + "），请安排统一配送至自提点。");
            log.info("团购已成团: activityId={}, joined={}/{}",
                    activity.getId(), activity.getJoinedNum(), activity.getMinNum());
        } else {
            // b) 未成团 → 流团：批量取消订单、模拟退款、成员置已退款、逐个通知
            activity.setStatus(STATUS_FAILED);
            this.updateById(activity);

            List<GroupBuyMember> members = groupBuyMemberMapper.selectList(
                    new LambdaQueryWrapper<GroupBuyMember>()
                            .eq(GroupBuyMember::getActivityId, activity.getId())
                            .eq(GroupBuyMember::getStatus, MEMBER_PAID));
            for (GroupBuyMember member : members) {
                Orders order = ordersMapper.selectById(member.getOrderId());
                if (order != null) {
                    order.setStatus(OrderConstant.CANCELLED);
                    order.setPayStatus(OrderConstant.PAY_REFUND);
                    order.setCancelReason("团购未成团，自动退款");
                    order.setCancelTime(LocalDateTime.now());
                    order.setUpdateTime(LocalDateTime.now());
                    ordersMapper.updateById(order);
                }
                member.setStatus(MEMBER_REFUND);
                groupBuyMemberMapper.updateById(member);
                noticeService.send(member.getUserId(), NOTICE_REFUND, "团购流团退款通知",
                        "您参与的【" + productName + "】团购未成团，订单已自动取消并退款，款项将原路返回。");
            }
            log.info("团购流团: activityId={}, joined={}/{}, 退款 {} 单",
                    activity.getId(), activity.getJoinedNum(), activity.getMinNum(), members.size());
        }
        // 删除详情缓存
        redisTemplate.delete(RedisCacheConstant.GROUP_BUY_PREFIX + activity.getId());
    }

    /**
     * 组装VO：商品/团长/自提点信息 + 剩余名额 + 进度
     */
    private GroupBuyActivityVO buildVO(GroupBuyActivity activity) {
        GroupBuyActivityVO vo = BeanUtil.copyProperties(activity, GroupBuyActivityVO.class);

        Product product = productMapper.selectById(activity.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setProductImage(product.getImage());
            vo.setOriginalPrice(product.getPrice());
        }
        User leader = userMapper.selectById(activity.getLeaderId());
        if (leader != null) {
            vo.setLeaderName(leader.getName());
        }
        PickupPoint point = pickupPointMapper.selectById(activity.getPickupPointId());
        if (point != null) {
            vo.setPointName(point.getName());
        }

        int minNum = activity.getMinNum() == null ? 0 : activity.getMinNum();
        int joinedNum = activity.getJoinedNum() == null ? 0 : activity.getJoinedNum();
        vo.setRemainNum(Math.max(minNum - joinedNum, 0));
        vo.setProgress(minNum == 0 ? 0 : Math.min(joinedNum * 100 / minNum, 100));

        vo.setStatusDesc(switch (activity.getStatus() == null ? -1 : activity.getStatus()) {
            case 0 -> "未开始";
            case 1 -> "进行中";
            case 2 -> "已成团";
            case 3 -> "已完成";
            case 4 -> "已流团";
            default -> "未知";
        });
        return vo;
    }
}
