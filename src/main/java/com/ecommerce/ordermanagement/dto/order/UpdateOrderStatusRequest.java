package com.ecommerce.ordermanagement.dto.order;

import com.ecommerce.ordermanagement.domain.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOrderStatusRequest(
        @NotNull OrderStatus status,
        @Size(max = 500) String comment
) {
}
