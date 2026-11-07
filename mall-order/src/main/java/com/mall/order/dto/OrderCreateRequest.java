package com.mall.order.dto;

import jakarta.validation.Valid;

import java.util.List;

public record OrderCreateRequest(List<@Valid OrderItemRequest> items) {
}
