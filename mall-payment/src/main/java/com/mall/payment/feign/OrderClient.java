package com.mall.payment.feign;

import com.mall.common.core.Result;
import com.mall.payment.dto.OrderDTO;
import com.mall.payment.dto.OrderPaidRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mall-order")
public interface OrderClient {

    @GetMapping("/internal/orders/{orderNo}")
    Result<OrderDTO> getOrder(@PathVariable("orderNo") String orderNo);

    @PostMapping("/internal/orders/{orderNo}/paid")
    Result<Void> markPaid(@PathVariable("orderNo") String orderNo,
                          @RequestBody OrderPaidRequest request);
}
