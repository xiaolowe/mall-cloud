package com.mall.product.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailVO(
        Long id,
        Long categoryId,
        String name,
        String subtitle,
        String coverUrl,
        String description,
        BigDecimal price,
        List<SkuVO> skus
) {
}
