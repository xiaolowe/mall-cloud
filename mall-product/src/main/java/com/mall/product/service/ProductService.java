package com.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.core.BizException;
import com.mall.common.core.PageResult;
import com.mall.product.dto.ProductDetailVO;
import com.mall.product.dto.ProductVO;
import com.mall.product.dto.SkuVO;
import com.mall.product.entity.Product;
import com.mall.product.entity.ProductSku;
import com.mall.product.mapper.ProductMapper;
import com.mall.product.mapper.ProductSkuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final ProductSkuMapper skuMapper;

    public PageResult<ProductVO> page(int page, int size, String keyword, Long categoryId) {
        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1);
        if (categoryId != null) {
            query.eq(Product::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.isBlank()) {
            query.and(w -> w.like(Product::getName, keyword).or().like(Product::getSubtitle, keyword));
        }
        query.orderByDesc(Product::getId);
        Page<Product> result = productMapper.selectPage(new Page<>(page, size), query);
        List<ProductVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(result.getTotal(), records);
    }

    @Cacheable(value = "product:detail", key = "#productId")
    public ProductDetailVO detail(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new BizException("商品不存在或已下架");
        }
        List<SkuVO> skus = skuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, productId)
                        .orderByAsc(ProductSku::getId))
                .stream()
                .map(sku -> toSkuVO(sku, product))
                .toList();
        return new ProductDetailVO(product.getId(), product.getCategoryId(), product.getName(),
                product.getSubtitle(), product.getCoverUrl(), product.getDescription(),
                product.getPrice(), skus);
    }

    public List<SkuVO> findSkus(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<ProductSku> skus = skuMapper.selectBatchIds(ids);
        if (skus.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> productIds = skus.stream().map(ProductSku::getProductId).distinct().toList();
        List<Product> products = productMapper.selectBatchIds(productIds);
        return skus.stream().map(sku -> {
            Product product = products.stream()
                    .filter(p -> p.getId().equals(sku.getProductId()))
                    .findFirst()
                    .orElse(null);
            return toSkuVO(sku, product);
        }).toList();
    }

    private ProductVO toVO(Product product) {
        return new ProductVO(product.getId(), product.getCategoryId(), product.getName(),
                product.getSubtitle(), product.getCoverUrl(), product.getPrice());
    }

    private SkuVO toSkuVO(ProductSku sku, Product product) {
        return new SkuVO(sku.getId(), sku.getProductId(),
                product == null ? null : product.getName(),
                sku.getTitle(), sku.getPrice(),
                product == null ? null : product.getCoverUrl(),
                sku.getSpecJson());
    }
}
