package com.ecommerce.ordermanagement.dto.cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long id,
        List<CartItemResponse> items,
        BigDecimal totalAmount,
        int totalItems
) {
}
