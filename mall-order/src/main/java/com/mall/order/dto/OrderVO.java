package com.mall.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderVO(
        Long id,
        String orderNo,
        Long userId,
        BigDecimal totalAmount,
        Integer status,
        String statusText,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        List<OrderItemVO> items
) {
}
