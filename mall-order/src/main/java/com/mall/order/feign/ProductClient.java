package com.mall.order.feign;

import com.mall.common.core.Result;
import com.mall.order.dto.SkuDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "mall-product")
public interface ProductClient {

    @GetMapping("/internal/skus")
    Result<List<SkuDTO>> findSkus(@RequestParam("ids") String ids);
}
