package com.mall.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.core.BizException;
import com.mall.common.core.Result;
import com.mall.common.web.UserContext;
import com.mall.order.dto.CartAddRequest;
import com.mall.order.dto.CartItemVO;
import com.mall.order.dto.SkuDTO;
import com.mall.order.entity.CartItem;
import com.mall.order.feign.ProductClient;
import com.mall.order.mapper.CartItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductClient productClient;

    public List<CartItemVO> list() {
        Long userId = UserContext.requireUserId();
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getId));
        if (items.isEmpty()) {
            return List.of();
        }
        String ids = items.stream()
                .map(item -> String.valueOf(item.getSkuId()))
                .distinct()
                .collect(Collectors.joining(","));
        Result<List<SkuDTO>> result = productClient.findSkus(ids);
        if (!result.isSuccess() || result.getData() == null) {
            throw new BizException("商品服务暂不可用，请稍后重试");
        }
        Map<Long, SkuDTO> skus = result.getData().stream()
                .collect(Collectors.toMap(SkuDTO::id, sku -> sku, (a, b) -> a));
        return items.stream()
                .map(item -> {
                    SkuDTO sku = skus.get(item.getSkuId());
                    if (sku == null) {
                        return null;
                    }
                    BigDecimal subtotal = sku.price().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return new CartItemVO(sku.id(), sku.productId(), sku.productName(), sku.title(),
                            sku.coverUrl(), sku.price(), item.getQuantity(), subtotal);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public void add(CartAddRequest request) {
        Long userId = UserContext.requireUserId();
        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSkuId, request.skuId())
                .last("limit 1"));
        if (existing == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setSkuId(request.skuId());
            item.setQuantity(request.quantity());
            item.setCreatedAt(LocalDateTime.now());
            cartItemMapper.insert(item);
        } else {
            existing.setQuantity(existing.getQuantity() + request.quantity());
            cartItemMapper.updateById(existing);
        }
    }

    public void remove(Long skuId) {
        Long userId = UserContext.requireUserId();
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getSkuId, skuId));
    }
}
