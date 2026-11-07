package com.mall.order.dto;

import java.math.BigDecimal;

public record SkuDTO(
        Long id,
        Long productId,
        String productName,
        String title,
        BigDecimal price,
        String coverUrl,
        String specJson
) {
}
