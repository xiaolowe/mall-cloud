package com.mall.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InventoryRequest(
        @NotBlank(message = "订单号不能为空") String orderNo,
        @NotEmpty(message = "库存明细不能为空") List<@Valid InventoryItem> items
) {
}
