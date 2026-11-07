package com.mall.product.dto;

import java.math.BigDecimal;

public record SkuVO(
        Long id,
        Long productId,
        String productName,
        String title,
        BigDecimal price,
        String coverUrl,
        String specJson
) {
}
