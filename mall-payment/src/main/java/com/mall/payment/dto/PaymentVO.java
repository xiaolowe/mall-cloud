package com.mall.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentVO(
        String paymentNo,
        String orderNo,
        BigDecimal amount,
        Integer status,
        String statusText,
        String channel,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {
}
