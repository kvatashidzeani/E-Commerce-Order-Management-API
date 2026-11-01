package com.ecommerce.ordermanagement.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @Size(max = 255) String name,
        @Size(max = 5000) String description,
        @Size(max = 500) String imageUrl,
        @DecimalMin(value = "0.00", inclusive = true) BigDecimal price,
        @Min(0) Integer stockQuantity,
        Long categoryId,
        Boolean active
) {
}
