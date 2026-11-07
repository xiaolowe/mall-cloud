package com.mall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.core.BizException;
import com.mall.common.core.PageResult;
import com.mall.product.dto.CategoryRequest;
import com.mall.product.dto.ProductSaveRequest;
import com.mall.product.dto.SkuSaveRequest;
import com.mall.product.entity.Category;
import com.mall.product.entity.Product;
import com.mall.product.entity.ProductSku;
import com.mall.product.feign.InventoryClient;
import com.mall.product.mapper.CategoryMapper;
import com.mall.product.mapper.ProductMapper;
import com.mall.product.mapper.ProductSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper skuMapper;
    private final InventoryClient inventoryClient;

    // ---------- category ----------

    public List<Category> listCategories() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));
    }

    public Category createCategory(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setSort(request.sort() == null ? 0 : request.sort());
        categoryMapper.insert(category);
        return category;
    }

    public Category updateCategory(Long id, CategoryRequest request) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BizException("分类不存在");
        }
        category.setName(request.name());
        if (request.sort() != null) {
            category.setSort(request.sort());
        }
        categoryMapper.updateById(category);
        return category;
    }

    public void deleteCategory(Long id) {
        Long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
        if (count > 0) {
            throw new BizException("该分类下还有商品，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    // ---------- product ----------

    public PageResult<Product> pageProducts(int page, int size, String keyword, Long categoryId, Integer status) {
        LambdaQueryWrapper<Product> query = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            query.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            query.eq(Product::getStatus, status);
        }
        if (keyword != null && !keyword.isBlank()) {
            query.like(Product::getName, keyword);
        }
        query.orderByDesc(Product::getId);
        Page<Product> result = productMapper.selectPage(new Page<>(page, size), query);
        return PageResult.of(result.getTotal(), result.getRecords());
    }

    public Product getProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BizException("商品不存在");
        }
        return product;
    }

    @CacheEvict(value = "product:detail", key = "#result.id")
    public Product createProduct(ProductSaveRequest request) {
        Product product = new Product();
        apply(product, request);
        product.setStatus(request.status() == null ? 1 : request.status());
        product.setCreatedAt(LocalDateTime.now());
        productMapper.insert(product);
        return product;
    }

    @CacheEvict(value = "product:detail", key = "#id")
    public Product updateProduct(Long id, ProductSaveRequest request) {
        Product product = getProduct(id);
        apply(product, request);
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        productMapper.updateById(product);
        return product;
    }

    @CacheEvict(value = "product:detail", key = "#id")
    public void updateStatus(Long id, int status) {
        Product product = getProduct(id);
        product.setStatus(status);
        productMapper.updateById(product);
    }

    @Transactional
    @CacheEvict(value = "product:detail", key = "#id")
    public void deleteProduct(Long id) {
        getProduct(id);
        skuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));
        productMapper.deleteById(id);
    }

    private void apply(Product product, ProductSaveRequest request) {
        product.setCategoryId(request.categoryId());
        product.setName(request.name());
        product.setSubtitle(request.subtitle());
        product.setCoverUrl(request.coverUrl());
        product.setDescription(request.description());
        product.setPrice(request.price());
    }

    // ---------- sku ----------

    public List<ProductSku> listSkus(Long productId) {
        return skuMapper.selectList(new LambdaQueryWrapper<ProductSku>()
                .eq(ProductSku::getProductId, productId)
                .orderByAsc(ProductSku::getId));
    }

    @CacheEvict(value = "product:detail", key = "#request.productId")
    public ProductSku createSku(SkuSaveRequest request) {
        getProduct(request.productId());
        ProductSku sku = new ProductSku();
        sku.setProductId(request.productId());
        sku.setTitle(request.title());
        sku.setPrice(request.price());
        sku.setSpecJson(request.specJson());
        sku.setCreatedAt(LocalDateTime.now());
        skuMapper.insert(sku);
        int initial = request.initialStock() == null ? 0 : request.initialStock();
        try {
            inventoryClient.init(sku.getId(), initial);
        } catch (Exception e) {
            log.warn("Failed to initialize stock for sku {}: {}", sku.getId(), e.getMessage());
        }
        return sku;
    }

    public ProductSku updateSku(Long id, SkuSaveRequest request) {
        ProductSku sku = skuMapper.selectById(id);
        if (sku == null) {
            throw new BizException("SKU 不存在");
        }
        sku.setTitle(request.title());
        sku.setPrice(request.price());
        sku.setSpecJson(request.specJson());
        skuMapper.updateById(sku);
        evictProduct(sku.getProductId());
        return sku;
    }

    public void deleteSku(Long id) {
        ProductSku sku = skuMapper.selectById(id);
        if (sku == null) {
            return;
        }
        skuMapper.deleteById(id);
        evictProduct(sku.getProductId());
    }

    @CacheEvict(value = "product:detail", key = "#productId")
    public void evictProduct(Long productId) {
        // cache eviction only
    }
}
