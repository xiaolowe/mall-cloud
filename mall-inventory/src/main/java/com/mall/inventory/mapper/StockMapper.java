package com.mall.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.inventory.entity.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface StockMapper extends BaseMapper<Stock> {

    @Update("UPDATE stock SET available = available - #{quantity}, updated_at = NOW() "
            + "WHERE sku_id = #{skuId} AND available >= #{quantity}")
    int deduct(@Param("skuId") Long skuId, @Param("quantity") int quantity);

    @Update("UPDATE stock SET available = available + #{quantity}, updated_at = NOW() "
            + "WHERE sku_id = #{skuId}")
    int restore(@Param("skuId") Long skuId, @Param("quantity") int quantity);
}
