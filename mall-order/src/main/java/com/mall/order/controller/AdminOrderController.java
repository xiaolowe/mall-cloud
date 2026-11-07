package com.mall.order.controller;

import com.mall.common.core.PageResult;
import com.mall.common.core.Result;
import com.mall.order.dto.OrderVO;
import com.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<PageResult<OrderVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo) {
        return Result.ok(orderService.adminList(page, size, status, orderNo));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(orderService.adminStats());
    }

    @GetMapping("/{orderNo}")
    public Result<OrderVO> get(@PathVariable String orderNo) {
        return Result.ok(orderService.adminGet(orderNo));
    }

    @PostMapping("/{orderNo}/ship")
    public Result<Void> ship(@PathVariable String orderNo) {
        orderService.adminShip(orderNo);
        return Result.ok();
    }

    @PostMapping("/{orderNo}/close")
    public Result<Void> close(@PathVariable String orderNo) {
        orderService.adminClose(orderNo);
        return Result.ok();
    }
}
