package com.mall.product.dto;

import java.math.BigDecimal;

public record ProductVO(
        Long id,
        Long categoryId,
        String name,
        String subtitle,
        String coverUrl,
        BigDecimal price
) {
}
