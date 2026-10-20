package com.fresh.modules.presale.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.constant.RedisCacheConstant;
import com.fresh.common.exception.BusinessException;
import com.fresh.common.result.PageResult;
import com.fresh.modules.presale.dto.PresaleActivityDTO;
import com.fresh.modules.presale.dto.PresalePageQueryDTO;
import com.fresh.modules.presale.entity.PresaleActivity;
import com.fresh.modules.presale.mapper.PresaleActivityMapper;
import com.fresh.modules.presale.service.PresaleService;
import com.fresh.modules.presale.vo.PresaleActivityVO;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预售活动 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PresaleServiceImpl extends ServiceImpl<PresaleActivityMapper, PresaleActivity>
        implements PresaleService {

    /** 活动状态：进行中 */
    private static final Integer STATUS_ONGOING = 1;
    /** 活动状态：已结束 */
    private static final Integer STATUS_FINISHED = 2;

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public PageResult pageQuery(PresalePageQueryDTO dto) {
        Page<PresaleActivity> page = new Page<>(dto.getPage(), dto.getPageSize());
        LambdaQueryWrapper<PresaleActivity> wrapper = new LambdaQueryWrapper<PresaleActivity>()
                .eq(dto.getStatus() != null, PresaleActivity::getStatus, dto.getStatus())
                .orderByDesc(PresaleActivity::getCreateTime);
        this.page(page, wrapper);

        List<PresaleActivityVO> voList = page.getRecords().stream()
                .map(this::buildVO)
                .collect(Collectors.toList());
        return new PageResult(page.getTotal(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(PresaleActivityDTO dto) {
        Product product = checkAndGetProduct(dto.getProductId());
        checkTime(dto.getStartTime(), dto.getEndTime());
        if (dto.getPresalePrice() == null || dto.getPresalePrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("预售价必须大于0");
        }
        if (dto.getStockLimit() == null || dto.getStockLimit() <= 0) {
            throw new BusinessException("预售库存必须大于0");
        }

        PresaleActivity activity = BeanUtil.copyProperties(dto, PresaleActivity.class);
        activity.setSoldCount(0);
        // 开始时间在未来则未开始，否则直接进行中
        activity.setStatus(dto.getStartTime().isAfter(LocalDateTime.now()) ? 0 : STATUS_ONGOING);
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());
        this.save(activity);

        // 商品标记为预售
        product.setIsPresale(1);
        productMapper.updateById(product);

        // 写Redis缓存
        redisTemplate.opsForValue().set(RedisCacheConstant.PRESALE_PREFIX + activity.getId(), activity);
        log.info("发布预售活动成功: id={}, productId={}", activity.getId(), activity.getProductId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PresaleActivityDTO dto) {
        PresaleActivity db = this.getById(dto.getId());
        if (db == null) {
            throw new BusinessException("预售活动不存在");
        }
        if (STATUS_FINISHED.equals(db.getStatus())) {
            throw new BusinessException("活动已结束，禁止修改");
        }
        checkTime(dto.getStartTime(), dto.getEndTime());

        PresaleActivity activity = BeanUtil.copyProperties(dto, PresaleActivity.class);
        // 已售数量不允许通过接口篡改
        activity.setSoldCount(null);
        activity.setStatus(null);
        activity.setUpdateTime(LocalDateTime.now());
        this.updateById(activity);

        // 刷新Redis缓存
        redisTemplate.opsForValue().set(RedisCacheConstant.PRESALE_PREFIX + db.getId(), this.getById(db.getId()));
    }

    @Override
    public void close(Long id) {
        PresaleActivity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException("预售活动不存在");
        }
        if (STATUS_FINISHED.equals(activity.getStatus())) {
            throw new BusinessException("活动已结束");
        }
        activity.setStatus(STATUS_FINISHED);
        activity.setUpdateTime(LocalDateTime.now());
        this.updateById(activity);
        // 删除Redis缓存
        redisTemplate.delete(RedisCacheConstant.PRESALE_PREFIX + id);
        log.info("预售活动手动结束: id={}", id);
    }

    @Override
    public void deleteById(Long id) {
        PresaleActivity activity = this.getById(id);
        if (activity == null) {
            throw new BusinessException("预售活动不存在");
        }
        if (STATUS_ONGOING.equals(activity.getStatus())) {
            throw new BusinessException("进行中的活动禁止删除，请先结束活动");
        }
        this.removeById(id);
        redisTemplate.delete(RedisCacheConstant.PRESALE_PREFIX + id);
    }

    @Override
    public List<PresaleActivityVO> listOngoing() {
        // 进行中且未到截止时间（Redis按活动维度缓存于详情接口，列表直接查库保证实时性）
        List<PresaleActivity> list = this.list(new LambdaQueryWrapper<PresaleActivity>()
                .eq(PresaleActivity::getStatus, STATUS_ONGOING)
                .gt(PresaleActivity::getEndTime, LocalDateTime.now())
                .orderByAsc(PresaleActivity::getEndTime));
        return list.stream().map(this::buildVO).collect(Collectors.toList());
    }

    @Override
    public PresaleActivityVO detail(Long id) {
        // 优先读Redis
        Object cached = redisTemplate.opsForValue().get(RedisCacheConstant.PRESALE_PREFIX + id);
        PresaleActivity activity;
        if (cached instanceof PresaleActivity pa) {
            activity = pa;
        } else {
            activity = this.getById(id);
            if (activity == null) {
                throw new BusinessException("预售活动不存在");
            }
            redisTemplate.opsForValue().set(RedisCacheConstant.PRESALE_PREFIX + id, activity);
        }
        return buildVO(activity);
    }

    /**
     * 校验商品存在
     */
    private Product checkAndGetProduct(Long productId) {
        if (productId == null) {
            throw new BusinessException("商品不能为空");
        }
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    /**
     * 校验活动时间合法
     */
    private void checkTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
            throw new BusinessException("活动时间不合法");
        }
    }

    /**
     * 组装VO：补商品信息与剩余库存、状态描述
     */
    private PresaleActivityVO buildVO(PresaleActivity activity) {
        PresaleActivityVO vo = BeanUtil.copyProperties(activity, PresaleActivityVO.class);
        Product product = productMapper.selectById(activity.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setProductImage(product.getImage());
        }
        int remain = (activity.getStockLimit() == null ? 0 : activity.getStockLimit())
                - (activity.getSoldCount() == null ? 0 : activity.getSoldCount());
        vo.setRemainStock(Math.max(remain, 0));
        vo.setStatusDesc(switch (activity.getStatus() == null ? -1 : activity.getStatus()) {
            case 0 -> "未开始";
            case 1 -> "进行中";
            case 2 -> "已结束";
            default -> "未知";
        });
        return vo;
    }
}
