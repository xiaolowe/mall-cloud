package com.mall.product.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "分类名称不能为空") String name,
        Integer sort
) {
}
