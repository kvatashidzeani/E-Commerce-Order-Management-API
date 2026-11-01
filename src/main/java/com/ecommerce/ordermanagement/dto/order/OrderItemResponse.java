package com.ecommerce.ordermanagement.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal unitPriceAtPurchase,
        Integer quantity,
        BigDecimal lineTotal
) {
}
