package com.ecommerce.ordermanagement.dto.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String imageUrl,
        String sku,
        BigDecimal price,
        Integer stockQuantity,
        Long categoryId,
        String categoryName,
        boolean active,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
