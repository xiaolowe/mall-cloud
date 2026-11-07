package com.mall.product.feign;

import com.mall.common.core.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mall-inventory")
public interface InventoryClient {

    @PostMapping("/internal/inventory/init")
    Result<Void> init(@RequestParam("skuId") Long skuId, @RequestParam("quantity") int quantity);
}
