package com.mall.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductSaveRequest(
        @NotNull(message = "分类不能为空") Long categoryId,
        @NotBlank(message = "商品名称不能为空") String name,
        String subtitle,
        String coverUrl,
        String description,
        @NotNull(message = "价格不能为空")
        @PositiveOrZero(message = "价格不能为负") BigDecimal price,
        Integer status
) {
}
