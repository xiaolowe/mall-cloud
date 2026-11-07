package com.mall.order.controller;

import com.mall.common.core.Result;
import com.mall.order.dto.OrderPaidRequest;
import com.mall.order.dto.OrderVO;
import com.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service-to-service endpoints. The gateway only routes /api/**, so these are not exposed publicly.
 */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @GetMapping("/{orderNo}")
    public Result<OrderVO> get(@PathVariable String orderNo) {
        return Result.ok(orderService.getInternal(orderNo));
    }

    @PostMapping("/{orderNo}/paid")
    public Result<Void> paid(@PathVariable String orderNo,
                             @RequestBody(required = false) OrderPaidRequest request) {
        orderService.markPaid(orderNo, request == null ? null : request.paymentNo());
        return Result.ok();
    }
}
