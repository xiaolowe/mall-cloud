package com.mall.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mall.common.core.Constants;
import com.mall.order.entity.Order;
import com.mall.order.mapper.OrderMapper;
import com.mall.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    @Value("${mall.order.timeout-minutes:30}")
    private long timeoutMinutes;

    @Scheduled(fixedDelayString = "${mall.order.scan-millis:60000}")
    public void cancelExpiredOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<Order> expired = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, Constants.ORDER_STATUS_PENDING_PAYMENT)
                .lt(Order::getCreatedAt, deadline)
                .last("limit 50"));
        for (Order order : expired) {
            int rows = orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                    .eq(Order::getId, order.getId())
                    .eq(Order::getStatus, Constants.ORDER_STATUS_PENDING_PAYMENT)
                    .set(Order::getStatus, Constants.ORDER_STATUS_CANCELLED)
                    .set(Order::getCancelReason, "超时未支付"));
            if (rows > 0) {
                orderService.restoreStock(order);
                log.info("Cancelled expired order {}", order.getOrderNo());
            }
        }
    }
}
