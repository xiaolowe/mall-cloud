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

    public void init(Long skuId, int quantity) {
        Stock existing = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>().eq(Stock::getSkuId, skuId));
        if (existing != null) {
            return;
        }
        Stock stock = new Stock();
        stock.setSkuId(skuId);
        stock.setTotal(quantity);
        stock.setAvailable(quantity);
        stock.setUpdatedAt(LocalDateTime.now());
        stockMapper.insert(stock);
    }

    @Transactional
    public StockVO adjust(Long skuId, int delta) {
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>().eq(Stock::getSkuId, skuId));
        if (stock == null) {
            throw new BizException("库存记录不存在，skuId=" + skuId);
        }
        int newAvailable = stock.getAvailable() + delta;
        int newTotal = stock.getTotal() + delta;
        if (newAvailable < 0) {
            throw new BizException("调整后可用库存不能为负");
        }
        stock.setAvailable(newAvailable);
        stock.setTotal(Math.max(newTotal, 0));
        stock.setUpdatedAt(LocalDateTime.now());
        stockMapper.updateById(stock);
        saveFlow(skuId, delta);
        return new StockVO(stock.getSkuId(), stock.getTotal(), stock.getAvailable());
    }

    public com.mall.common.core.PageResult<StockVO> pageStock(int page, int size) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Stock> result =
                stockMapper.selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size),
                        new LambdaQueryWrapper<Stock>().orderByAsc(Stock::getSkuId));
        return com.mall.common.core.PageResult.of(result.getTotal(),
                result.getRecords().stream()
                        .map(s -> new StockVO(s.getSkuId(), s.getTotal(), s.getAvailable()))
                        .toList());
    }

    private void saveFlow(Long skuId, int delta) {
        StockFlow flow = new StockFlow();
        flow.setOrderNo("ADMIN-" + System.currentTimeMillis());
        flow.setSkuId(skuId);
        flow.setQuantity(delta);
        flow.setType("ADJUST");
        flow.setCreatedAt(LocalDateTime.now());
        flowMapper.insert(flow);
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
