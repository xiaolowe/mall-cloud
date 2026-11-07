package com.mall.order.dto;

import java.math.BigDecimal;

public record CartItemVO(
        Long skuId,
        Long productId,
        String productName,
        String skuTitle,
        String coverUrl,
        BigDecimal price,
        Integer quantity,
        BigDecimal subtotal
) {
}
