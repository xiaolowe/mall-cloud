package com.mall.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "skuId 不能为空") Long skuId,
        @Min(value = 1, message = "购买数量至少为 1") int quantity
) {
}
