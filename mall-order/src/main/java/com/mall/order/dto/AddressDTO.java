package com.mall.order.dto;

public record AddressDTO(
        Long id,
        String receiverName,
        String receiverPhone,
        String detail,
        boolean isDefault
) {
}
