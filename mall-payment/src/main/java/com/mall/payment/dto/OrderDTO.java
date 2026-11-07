package com.mall.payment.dto;

import java.math.BigDecimal;

public record OrderDTO(String orderNo, Long userId, BigDecimal totalAmount, Integer status) {
}
