package com.mall.product.controller;

import com.mall.common.core.Result;
import com.mall.product.dto.SkuVO;
import com.mall.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Service-to-service endpoints. The gateway only routes /api/**, so these are not exposed publicly.
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @GetMapping("/skus")
    public Result<List<SkuVO>> skus(@RequestParam List<Long> ids) {
        return Result.ok(productService.findSkus(ids));
    }

    @GetMapping("/products/{id}")
    public Result<SkuVO> productSku(@PathVariable Long id) {
        return Result.ok(productService.findSkus(List.of(id)).stream().findFirst().orElse(null));
    }
}
