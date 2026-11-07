package com.mall.order.controller;

import com.mall.common.core.Result;
import com.mall.order.dto.CartAddRequest;
import com.mall.order.dto.CartItemVO;
import com.mall.order.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Result<List<CartItemVO>> list() {
        return Result.ok(cartService.list());
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody CartAddRequest request) {
        cartService.add(request);
        return Result.ok();
    }

    @DeleteMapping("/{skuId}")
    public Result<Void> remove(@PathVariable Long skuId) {
        cartService.remove(skuId);
        return Result.ok();
    }
}
