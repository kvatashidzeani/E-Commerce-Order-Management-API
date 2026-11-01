package com.ecommerce.ordermanagement.dto.order;

import com.ecommerce.ordermanagement.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        OrderStatus status,
        BigDecimal totalAmount,
        String shippingRecipient,
        String shippingPhone,
        String shippingCountry,
        String shippingCity,
        String shippingStreet,
        String shippingPostalCode,
        List<OrderItemResponse> items,
        List<OrderStatusHistoryResponse> statusHistory,
        Instant paidAt,
        Instant shippedAt,
        Instant deliveredAt,
        Instant cancelledAt,
        Instant createdAt
) {
}
