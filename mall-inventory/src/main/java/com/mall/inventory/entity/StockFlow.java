package com.mall.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stock_flow")
public class StockFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long skuId;

    private Integer quantity;

    private String type;

    private LocalDateTime createdAt;
}
