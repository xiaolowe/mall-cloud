package com.mall.payment.controller;

import com.mall.common.core.Result;
import com.mall.payment.dto.PaymentVO;
import com.mall.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderNo}/pay")
    public Result<PaymentVO> pay(@PathVariable String orderNo) {
        return Result.ok(paymentService.pay(orderNo));
    }

    @GetMapping("/{orderNo}")
    public Result<PaymentVO> get(@PathVariable String orderNo) {
        return Result.ok(paymentService.getByOrderNo(orderNo));
    }
}
