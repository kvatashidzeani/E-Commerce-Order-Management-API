package com.ecommerce.ordermanagement.dto.cart;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String sku,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal,
        Integer availableStock
) {
}
