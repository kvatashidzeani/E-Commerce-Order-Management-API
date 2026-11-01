package com.ecommerce.ordermanagement.dto.order;

import com.ecommerce.ordermanagement.domain.enums.OrderStatus;

import java.time.Instant;

public record OrderStatusHistoryResponse(
        OrderStatus fromStatus,
        OrderStatus toStatus,
        Long changedByUserId,
        Instant changedAt,
        String comment
) {
}
