package com.mall.inventory.controller;

import com.mall.common.core.Result;
import com.mall.inventory.dto.StockVO;
import com.mall.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{skuId}")
    public Result<StockVO> stock(@PathVariable Long skuId) {
        return Result.ok(inventoryService.getStock(skuId));
    }
}
