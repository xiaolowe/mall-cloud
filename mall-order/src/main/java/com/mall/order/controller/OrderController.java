package com.mall.order.controller;

import com.mall.common.core.PageResult;
import com.mall.common.core.Result;
import com.mall.order.dto.OrderCreateRequest;
import com.mall.order.dto.OrderVO;
import com.mall.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody(required = false) OrderCreateRequest request) {
        return Result.ok(orderService.create(request));
    }

    @GetMapping
    public Result<PageResult<OrderVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(orderService.list(page, size));
    }

    @GetMapping("/{orderNo}")
    public Result<OrderVO> get(@PathVariable String orderNo) {
        return Result.ok(orderService.get(orderNo));
    }

    @PostMapping("/{orderNo}/cancel")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.cancel(orderNo);
        return Result.ok();
    }
}
