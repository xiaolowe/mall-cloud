package com.mall.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record SkuSaveRequest(
        @NotNull(message = "商品不能为空") Long productId,
        @NotBlank(message = "SKU 标题不能为空") String title,
        @NotNull(message = "价格不能为空")
        @PositiveOrZero(message = "价格不能为负") BigDecimal price,
        String specJson,
        @PositiveOrZero(message = "初始库存不能为负") Integer initialStock
) {
}
