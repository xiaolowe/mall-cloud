package com.mall.order.dto;

import java.util.List;

public record InventoryRequestDTO(String orderNo, List<Item> items) {

    public record Item(Long skuId, int quantity) {
    }
}
