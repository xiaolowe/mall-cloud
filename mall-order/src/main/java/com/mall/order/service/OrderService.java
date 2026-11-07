package com.mall.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.core.BizException;
import com.mall.common.core.Constants;
import com.mall.common.core.PageResult;
import com.mall.common.core.Result;
import com.mall.common.web.UserContext;
import com.mall.order.dto.AddressDTO;
import com.mall.order.dto.InventoryRequestDTO;
import com.mall.order.dto.OrderCreateRequest;
import com.mall.order.dto.OrderItemRequest;
import com.mall.order.dto.OrderItemVO;
import com.mall.order.dto.OrderLine;
import com.mall.order.dto.OrderVO;
import com.mall.order.dto.SkuDTO;
import com.mall.order.entity.CartItem;
import com.mall.order.entity.Order;
import com.mall.order.entity.OrderItem;
import com.mall.order.feign.InventoryClient;
import com.mall.order.feign.ProductClient;
import com.mall.order.feign.UserClient;
import com.mall.order.mapper.CartItemMapper;
import com.mall.order.mapper.OrderItemMapper;
import com.mall.order.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;
    private final ProductClient productClient;
    private final InventoryClient inventoryClient;
    private final UserClient userClient;
    private final OrderTxService orderTxService;

    public OrderVO create(OrderCreateRequest request) {
        Long userId = UserContext.requireUserId();
        boolean fromCart = request == null || request.items() == null || request.items().isEmpty();
        List<OrderLine> lines = resolveLines(userId, request);
        Map<Long, SkuDTO> skus = loadSkus(lines);
        AddressDTO address = loadAddress(userId);
        BigDecimal totalAmount = calculateTotal(lines, skus);
        String orderNo = generateOrderNo();

        InventoryRequestDTO inventoryRequest = new InventoryRequestDTO(orderNo,
                lines.stream().map(l -> new InventoryRequestDTO.Item(l.skuId(), l.quantity())).toList());
        Result<Void> deductResult = inventoryClient.deduct(inventoryRequest);
        if (!deductResult.isSuccess()) {
            throw new BizException(deductResult.getCode(), deductResult.getMessage());
        }

        try {
            orderTxService.saveOrder(userId, orderNo, totalAmount, address, lines, skus, fromCart);
        } catch (RuntimeException e) {
            try {
                inventoryClient.restore(inventoryRequest);
            } catch (Exception restoreError) {
                log.error("Failed to restore stock for order {}", orderNo, restoreError);
            }
            throw e;
        }
        return get(userId, orderNo);
    }

    public PageResult<OrderVO> list(int page, int size) {
        Long userId = UserContext.requireUserId();
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getUserId, userId)
                        .orderByDesc(Order::getId));
        List<OrderVO> records = result.getRecords().stream()
                .map(order -> toVO(order, findItems(order.getId())))
                .toList();
        return PageResult.of(result.getTotal(), records);
    }

    public OrderVO get(String orderNo) {
        return get(UserContext.requireUserId(), orderNo);
    }

    public OrderVO get(Long userId, String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BizException("订单不存在");
        }
        return toVO(order, findItems(order.getId()));
    }

    public OrderVO getInternal(String orderNo) {
        Order order = findOrder(orderNo);
        return toVO(order, findItems(order.getId()));
    }

    public void cancel(String orderNo) {
        Long userId = UserContext.requireUserId();
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getOrderNo, orderNo)
                .eq(Order::getUserId, userId));
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (order.getStatus() != Constants.ORDER_STATUS_PENDING_PAYMENT) {
            throw new BizException("当前订单状态不允许取消");
        }
        restoreStock(order);
        int rows = orderMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.getId())
                .eq(Order::getStatus, Constants.ORDER_STATUS_PENDING_PAYMENT)
                .set(Order::getStatus, Constants.ORDER_STATUS_CANCELLED)
                .set(Order::getCancelReason, "用户取消"));
        if (rows == 0) {
            throw new BizException("订单状态已变更，请刷新后重试");
        }
    }

    @Transactional
    public void markPaid(String orderNo, String paymentNo) {
        Order order = findOrder(orderNo);
        if (order.getStatus() == Constants.ORDER_STATUS_PAID) {
            log.info("Order {} already paid, paymentNo={}", orderNo, paymentNo);
            return;
        }
        if (order.getStatus() != Constants.ORDER_STATUS_PENDING_PAYMENT) {
            throw new BizException("当前订单状态不允许支付");
        }
        int rows = orderMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, order.getId())
                        .eq(Order::getStatus, Constants.ORDER_STATUS_PENDING_PAYMENT)
                        .set(Order::getStatus, Constants.ORDER_STATUS_PAID)
                        .set(Order::getPaidAt, LocalDateTime.now()));
        if (rows == 0) {
            throw new BizException("订单状态已变更，请刷新后重试");
        }
        log.info("Order {} paid, paymentNo={}", orderNo, paymentNo);
    }

    public void restoreStock(Order order) {
        List<OrderItem> items = findItems(order.getId());
        if (items.isEmpty()) {
            return;
        }
        try {
            Result<Void> result = inventoryClient.restore(new InventoryRequestDTO(order.getOrderNo(),
                    items.stream()
                            .map(i -> new InventoryRequestDTO.Item(i.getSkuId(), i.getQuantity()))
                            .toList()));
            if (!result.isSuccess()) {
                log.warn("Restore stock failed for order {}: {}", order.getOrderNo(), result.getMessage());
            }
        } catch (Exception e) {
            log.error("Restore stock failed for order {}", order.getOrderNo(), e);
        }
    }

    private List<OrderLine> resolveLines(Long userId, OrderCreateRequest request) {
        List<OrderItemRequest> requested = request == null ? null : request.items();
        if (requested != null && !requested.isEmpty()) {
            return merge(requested.stream()
                    .map(item -> new OrderLine(item.skuId(), item.quantity()))
                    .toList());
        }
        List<CartItem> cartItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, userId));
        if (cartItems.isEmpty()) {
            throw new BizException("购物车为空，请先添加商品");
        }
        return merge(cartItems.stream()
                .map(item -> new OrderLine(item.getSkuId(), item.getQuantity()))
                .toList());
    }

    private List<OrderLine> merge(List<OrderLine> lines) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderLine line : lines) {
            quantities.merge(line.skuId(), line.quantity(), Integer::sum);
        }
        return quantities.entrySet().stream()
                .map(entry -> new OrderLine(entry.getKey(), entry.getValue()))
                .toList();
    }

    private Map<Long, SkuDTO> loadSkus(List<OrderLine> lines) {
        String ids = lines.stream()
                .map(line -> String.valueOf(line.skuId()))
                .collect(Collectors.joining(","));
        Result<List<SkuDTO>> result = productClient.findSkus(ids);
        if (!result.isSuccess() || result.getData() == null) {
            throw new BizException("商品服务暂不可用，请稍后重试");
        }
        Map<Long, SkuDTO> skus = result.getData().stream()
                .collect(Collectors.toMap(SkuDTO::id, sku -> sku, (a, b) -> a));
        for (OrderLine line : lines) {
            if (!skus.containsKey(line.skuId())) {
                throw new BizException("商品不存在或已下架，skuId=" + line.skuId());
            }
        }
        return skus;
    }

    private AddressDTO loadAddress(Long userId) {
        Result<AddressDTO> result = userClient.defaultAddress(userId);
        if (!result.isSuccess() || result.getData() == null) {
            throw new BizException(result.getCode() == 200 ? 400 : result.getCode(), result.getMessage());
        }
        return result.getData();
    }

    private BigDecimal calculateTotal(List<OrderLine> lines, Map<Long, SkuDTO> skus) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            BigDecimal price = skus.get(line.skuId()).price();
            total = total.add(price.multiply(BigDecimal.valueOf(line.quantity())));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(ORDER_NO_FORMAT)
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    private Order findOrder(String orderNo) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    private List<OrderItem> findItems(Long orderId) {
        return orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
    }

    private OrderVO toVO(Order order, List<OrderItem> items) {
        List<OrderItemVO> itemVOs = items.stream()
                .map(item -> new OrderItemVO(item.getSkuId(), item.getProductId(), item.getProductName(),
                        item.getSkuTitle(), item.getCoverUrl(), item.getPrice(), item.getQuantity()))
                .toList();
        return new OrderVO(order.getId(), order.getOrderNo(), order.getUserId(), order.getTotalAmount(),
                order.getStatus(), statusText(order.getStatus()), order.getReceiverName(),
                order.getReceiverPhone(), order.getReceiverAddress(), order.getCreatedAt(),
                order.getPaidAt(), itemVOs);
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case Constants.ORDER_STATUS_PENDING_PAYMENT -> "待支付";
            case Constants.ORDER_STATUS_PAID -> "已支付";
            case Constants.ORDER_STATUS_CANCELLED -> "已取消";
            case Constants.ORDER_STATUS_COMPLETED -> "已完成";
            default -> "未知";
        };
    }
}
