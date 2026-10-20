package com.fresh.modules.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fresh.common.constant.StatusConstant;
import com.fresh.common.context.BaseContext;
import com.fresh.common.exception.BusinessException;
import com.fresh.modules.cart.dto.ShoppingCartDTO;
import com.fresh.modules.cart.entity.ShoppingCart;
import com.fresh.modules.cart.mapper.ShoppingCartMapper;
import com.fresh.modules.cart.service.ShoppingCartService;
import com.fresh.modules.product.entity.Product;
import com.fresh.modules.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    private final ProductMapper productMapper;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Product product = productMapper.selectById(shoppingCartDTO.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        // 加购前校验商品起售状态
        if (!StatusConstant.ENABLE.equals(product.getStatus())) {
            throw new BusinessException("商品已停售，无法加入购物车");
        }

        Long userId = BaseContext.getCurrentId();
        BigDecimal weight = null;
        Integer number = 1;

        if (Product.PRICE_TYPE_PER.equals(product.getPriceType())) {
            // 按份商品必须 number > 0
            if (shoppingCartDTO.getNumber() == null || shoppingCartDTO.getNumber() <= 0) {
                throw new BusinessException("按份商品份数必须大于0");
            }
            number = shoppingCartDTO.getNumber();
        } else {
            // 按重量商品必须 weight > 0
            if (shoppingCartDTO.getWeight() == null
                    || shoppingCartDTO.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("按重量商品称重重量必须大于0");
            }
            weight = shoppingCartDTO.getWeight();
        }

        // 同一用户同商品同重量规格的购物车条目
        ShoppingCart exist = this.getOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getProductId, product.getId())
                .eq(weight != null, ShoppingCart::getWeight, weight)
                .isNull(weight == null, ShoppingCart::getWeight)
                .last("limit 1"));

        if (exist != null) {
            // 已存在则累加：按份累加份数，按重量累加重量，并重算小计金额
            if (weight != null) {
                BigDecimal newWeight = exist.getWeight().add(weight);
                exist.setWeight(newWeight);
                // 金额 = 单价 × 重量
                exist.setAmount(product.getPrice().multiply(newWeight));
            } else {
                int newNumber = exist.getNumber() + number;
                exist.setNumber(newNumber);
                // 金额 = 单价 × 份数
                exist.setAmount(product.getPrice().multiply(BigDecimal.valueOf(newNumber)));
            }
            this.updateById(exist);
        } else {
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setProductId(product.getId());
            cart.setName(product.getName());
            cart.setImage(product.getImage());
            cart.setWeight(weight);
            cart.setNumber(weight != null ? 1 : number);
            cart.setAmount(weight != null
                    ? product.getPrice().multiply(weight)
                    : product.getPrice().multiply(BigDecimal.valueOf(number)));
            cart.setCreateTime(LocalDateTime.now());
            this.save(cart);
        }
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        // 定位购物车条目：按重量商品需匹配重量规格
        ShoppingCart exist = this.getOne(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, userId)
                .eq(ShoppingCart::getProductId, shoppingCartDTO.getProductId())
                .eq(shoppingCartDTO.getWeight() != null, ShoppingCart::getWeight, shoppingCartDTO.getWeight())
                .isNull(shoppingCartDTO.getWeight() == null, ShoppingCart::getWeight)
                .last("limit 1"));
        if (exist == null) {
            throw new BusinessException("购物车中不存在该商品");
        }

        // 按份商品份数大于1则减一份，否则删除条目；按重量商品直接删除
        if (exist.getWeight() == null && exist.getNumber() > 1) {
            int newNumber = exist.getNumber() - 1;
            exist.setNumber(newNumber);
            Product product = productMapper.selectById(exist.getProductId());
            exist.setAmount(product.getPrice().multiply(BigDecimal.valueOf(newNumber)));
            this.updateById(exist);
        } else {
            this.removeById(exist.getId());
        }
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        return this.list(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId())
                .orderByDesc(ShoppingCart::getCreateTime));
    }

    @Override
    public void cleanShoppingCart() {
        this.remove(new LambdaQueryWrapper<ShoppingCart>()
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId()));
    }
}
