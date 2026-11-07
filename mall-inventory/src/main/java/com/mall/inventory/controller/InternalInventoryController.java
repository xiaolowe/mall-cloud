package com.mall.inventory.controller;

import com.mall.common.core.Result;
import com.mall.inventory.dto.InventoryRequest;
import com.mall.inventory.dto.StockVO;
import com.mall.inventory.service.InventoryService;
import jakarta.validation.Valid;
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
@RequestMapping("/internal/inventory")
@RequiredArgsConstructor
public class InternalInventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/deduct")
    public Result<Void> deduct(@Valid @RequestBody InventoryRequest request) {
        inventoryService.deduct(request);
        return Result.ok();
    }

    @PostMapping("/restore")
    public Result<Void> restore(@Valid @RequestBody InventoryRequest request) {
        inventoryService.restore(request);
        return Result.ok();
    }

    @GetMapping("/{skuId}")
    public Result<StockVO> stock(@PathVariable Long skuId) {
        return Result.ok(inventoryService.getStock(skuId));
    }
}
