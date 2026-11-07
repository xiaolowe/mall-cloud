package com.mall.order.feign;

import com.mall.common.core.Result;
import com.mall.order.dto.AddressDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "mall-user")
public interface UserClient {

    @GetMapping("/internal/users/{id}/addresses/default")
    Result<AddressDTO> defaultAddress(@PathVariable("id") Long userId);
}
