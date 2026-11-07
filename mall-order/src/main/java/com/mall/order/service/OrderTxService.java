package com.mall.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.order.dto.AddressDTO;
import com.mall.order.dto.OrderLine;
import com.mall.order.dto.SkuDTO;
import com.mall.order.entity.CartItem;
import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import com.mall.order.mapper.CartItemMapper;
import com.mall.order.mapper.OrderItemMapper;
import com.mall.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Writes order data in one local transaction. Inventory has already been deducted by the caller,
 * which compensates with a restore call if this transaction fails.
 */
@Service
@RequiredArgsConstructor
public class OrderTxService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;

    @Transactional
    public void saveOrder(Long userId, String orderNo, BigDecimal totalAmount, AddressDTO address,
                          List<OrderLine> lines, Map<Long, SkuDTO> skus, boolean clearCart) {
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        order.setReceiverName(address.receiverName());
        order.setReceiverPhone(address.receiverPhone());
        order.setReceiverAddress(address.detail());
        order.setCreatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        for (OrderLine line : lines) {
            SkuDTO sku = skus.get(line.skuId());
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setSkuId(sku.id());
            item.setProductId(sku.productId());
            item.setProductName(sku.productName());
            item.setSkuTitle(sku.title());
            item.setCoverUrl(sku.coverUrl());
            item.setPrice(sku.price());
            item.setQuantity(line.quantity());
            orderItemMapper.insert(item);
        }

        if (clearCart) {
            cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId, userId)
                    .in(CartItem::getSkuId, lines.stream().map(OrderLine::skuId).toList()));
        }
    }
}
