package com.mall.inventory.controller;

import com.mall.common.core.PageResult;
import com.mall.common.core.Result;
import com.mall.inventory.dto.StockVO;
import com.mall.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public Result<PageResult<StockVO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(inventoryService.pageStock(page, size));
    }

    @GetMapping("/{skuId}")
    public Result<StockVO> stock(@PathVariable Long skuId) {
        return Result.ok(inventoryService.getStock(skuId));
    }

    @PostMapping("/{skuId}/adjust")
    public Result<StockVO> adjust(@PathVariable Long skuId, @RequestParam int delta) {
        return Result.ok(inventoryService.adjust(skuId, delta));
    }
}
