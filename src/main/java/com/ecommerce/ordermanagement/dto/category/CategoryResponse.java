package com.ecommerce.ordermanagement.dto.category;

public record CategoryResponse(
        Long id,
        String name,
        Long parentId
) {
}
