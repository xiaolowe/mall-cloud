package com.mall.product.controller;

import com.mall.common.core.PageResult;
import com.mall.common.core.Result;
import com.mall.product.dto.CategoryRequest;
import com.mall.product.dto.ProductSaveRequest;
import com.mall.product.dto.SkuSaveRequest;
import com.mall.product.entity.Category;
import com.mall.product.entity.Product;
import com.mall.product.entity.ProductSku;
import com.mall.product.service.AdminProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminService;

    // ---------- category ----------

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.ok(adminService.listCategories());
    }

    @PostMapping("/categories")
    public Result<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        return Result.ok(adminService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public Result<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return Result.ok(adminService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return Result.ok();
    }

    // ---------- product ----------

    @GetMapping("/products")
    public Result<PageResult<Product>> products(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return Result.ok(adminService.pageProducts(page, size, keyword, categoryId, status));
    }

    @GetMapping("/products/{id}")
    public Result<Product> product(@PathVariable Long id) {
        return Result.ok(adminService.getProduct(id));
    }

    @PostMapping("/products")
    public Result<Product> createProduct(@Valid @RequestBody ProductSaveRequest request) {
        return Result.ok(adminService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductSaveRequest request) {
        return Result.ok(adminService.updateProduct(id, request));
    }

    @PutMapping("/products/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam int status) {
        adminService.updateStatus(id, status);
        return Result.ok();
    }

    @DeleteMapping("/products/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return Result.ok();
    }

    // ---------- sku ----------

    @GetMapping("/products/{productId}/skus")
    public Result<List<ProductSku>> skus(@PathVariable Long productId) {
        return Result.ok(adminService.listSkus(productId));
    }

    @PostMapping("/skus")
    public Result<ProductSku> createSku(@Valid @RequestBody SkuSaveRequest request) {
        return Result.ok(adminService.createSku(request));
    }

    @PutMapping("/skus/{id}")
    public Result<ProductSku> updateSku(@PathVariable Long id, @Valid @RequestBody SkuSaveRequest request) {
        return Result.ok(adminService.updateSku(id, request));
    }

    @DeleteMapping("/skus/{id}")
    public Result<Void> deleteSku(@PathVariable Long id) {
        adminService.deleteSku(id);
        return Result.ok();
    }
}
