package com.ecommerce.ordermanagement.dto.order;

import com.ecommerce.ordermanagement.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSummaryResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        int itemCount,
        Instant createdAt
) {
}
