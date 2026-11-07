package com.mall.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.core.BizException;
import com.mall.inventory.dto.InventoryItem;
import com.mall.inventory.dto.InventoryRequest;
import com.mall.inventory.dto.StockVO;
import com.mall.inventory.entity.Stock;
import com.mall.inventory.entity.StockFlow;
import com.mall.inventory.mapper.StockFlowMapper;
import com.mall.inventory.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final String TYPE_DEDUCT = "DEDUCT";
    private static final String TYPE_RESTORE = "RESTORE";

    private final StockMapper stockMapper;
    private final StockFlowMapper flowMapper;

    @Transactional
    public void deduct(InventoryRequest request) {
        for (InventoryItem item : request.items()) {
            if (hasFlow(request.orderNo(), item.skuId(), TYPE_DEDUCT)) {
                continue;
            }
            int rows = stockMapper.deduct(item.skuId(), item.quantity());
            if (rows == 0) {
                throw new BizException("商品库存不足，skuId=" + item.skuId());
            }
            saveFlow(request.orderNo(), item, TYPE_DEDUCT);
        }
    }

    @Transactional
    public void restore(InventoryRequest request) {
        for (InventoryItem item : request.items()) {
            if (!hasFlow(request.orderNo(), item.skuId(), TYPE_DEDUCT)
                    || hasFlow(request.orderNo(), item.skuId(), TYPE_RESTORE)) {
                continue;
            }
            stockMapper.restore(item.skuId(), item.quantity());
            saveFlow(request.orderNo(), item, TYPE_RESTORE);
        }
    }

    public StockVO getStock(Long skuId) {
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>().eq(Stock::getSkuId, skuId));
        if (stock == null) {
            throw new BizException("库存记录不存在，skuId=" + skuId);
        }
        return new StockVO(stock.getSkuId(), stock.getTotal(), stock.getAvailable());
    }

    private boolean hasFlow(String orderNo, Long skuId, String type) {
        return flowMapper.selectCount(new LambdaQueryWrapper<StockFlow>()
                .eq(StockFlow::getOrderNo, orderNo)
                .eq(StockFlow::getSkuId, skuId)
                .eq(StockFlow::getType, type)) > 0;
    }

    private void saveFlow(String orderNo, InventoryItem item, String type) {
        StockFlow flow = new StockFlow();
        flow.setOrderNo(orderNo);
        flow.setSkuId(item.skuId());
        flow.setQuantity(item.quantity());
        flow.setType(type);
        flow.setCreatedAt(LocalDateTime.now());
        flowMapper.insert(flow);
    }
}
