package com.mall.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.core.BizException;
import com.mall.common.core.Constants;
import com.mall.common.core.Result;
import com.mall.common.core.ResultCode;
import com.mall.common.web.UserContext;
import com.mall.payment.dto.OrderDTO;
import com.mall.payment.dto.OrderPaidRequest;
import com.mall.payment.dto.PaymentVO;
import com.mall.payment.entity.Payment;
import com.mall.payment.feign.OrderClient;
import com.mall.payment.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final DateTimeFormatter PAYMENT_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_FAILED = 2;

    private final PaymentMapper paymentMapper;
    private final OrderClient orderClient;

    public PaymentVO pay(String orderNo) {
        Long userId = UserContext.requireUserId();
        Result<OrderDTO> orderResult = orderClient.getOrder(orderNo);
        if (!orderResult.isSuccess() || orderResult.getData() == null) {
            throw new BizException(orderResult.getCode(), orderResult.getMessage());
        }
        OrderDTO order = orderResult.getData();
        if (!userId.equals(order.userId())) {
            throw new BizException(ResultCode.FORBIDDEN.getCode(), "无权支付该订单");
        }

        Payment successPayment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderNo, orderNo)
                .eq(Payment::getStatus, STATUS_SUCCESS)
                .last("limit 1"));
        if (successPayment != null) {
            return toVO(successPayment);
        }
        if (order.status() != null && order.status() == Constants.ORDER_STATUS_PAID) {
            throw new BizException("订单已支付");
        }
        if (order.status() == null || order.status() != Constants.ORDER_STATUS_PENDING_PAYMENT) {
            throw new BizException("订单状态不允许支付");
        }

        Payment payment = new Payment();
        payment.setPaymentNo(generatePaymentNo());
        payment.setOrderNo(orderNo);
        payment.setUserId(userId);
        payment.setAmount(order.totalAmount());
        payment.setChannel("MOCK");
        payment.setStatus(STATUS_PENDING);
        payment.setCreatedAt(LocalDateTime.now());
        paymentMapper.insert(payment);

        Result<Void> paidResult = orderClient.markPaid(orderNo, new OrderPaidRequest(payment.getPaymentNo()));
        if (!paidResult.isSuccess()) {
            payment.setStatus(STATUS_FAILED);
            paymentMapper.updateById(payment);
            throw new BizException(paidResult.getCode(), paidResult.getMessage());
        }

        payment.setStatus(STATUS_SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.updateById(payment);
        log.info("Mock payment success: orderNo={}, paymentNo={}, amount={}",
                orderNo, payment.getPaymentNo(), payment.getAmount());
        return toVO(payment);
    }

    public PaymentVO getByOrderNo(String orderNo) {
        Long userId = UserContext.requireUserId();
        Payment payment = paymentMapper.selectOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderNo, orderNo)
                .eq(Payment::getUserId, userId)
                .orderByDesc(Payment::getId)
                .last("limit 1"));
        if (payment == null) {
            throw new BizException("支付记录不存在");
        }
        return toVO(payment);
    }

    private String generatePaymentNo() {
        return "P" + LocalDateTime.now().format(PAYMENT_NO_FORMAT)
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    private PaymentVO toVO(Payment payment) {
        String statusText = switch (payment.getStatus()) {
            case STATUS_PENDING -> "支付中";
            case STATUS_SUCCESS -> "支付成功";
            case STATUS_FAILED -> "支付失败";
            default -> "未知";
        };
        return new PaymentVO(payment.getPaymentNo(), payment.getOrderNo(), payment.getAmount(),
                payment.getStatus(), statusText, payment.getChannel(),
                payment.getCreatedAt(), payment.getPaidAt());
    }
}
