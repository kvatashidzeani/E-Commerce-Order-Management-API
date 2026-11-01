package com.ecommerce.ordermanagement.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 5000) String description,
        @Size(max = 500) String imageUrl,
        @NotBlank @Size(max = 100) String sku,
        @NotNull @DecimalMin(value = "0.00", inclusive = true) BigDecimal price,
        @NotNull @Min(0) Integer stockQuantity,
        @NotNull Long categoryId,
        Boolean active
) {
}
