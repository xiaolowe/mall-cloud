package com.mall.order.feign;

import com.mall.common.core.Result;
import com.mall.order.dto.InventoryRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mall-inventory")
public interface InventoryClient {

    @PostMapping("/internal/inventory/deduct")
    Result<Void> deduct(@RequestBody InventoryRequestDTO request);

    @PostMapping("/internal/inventory/restore")
    Result<Void> restore(@RequestBody InventoryRequestDTO request);
}
