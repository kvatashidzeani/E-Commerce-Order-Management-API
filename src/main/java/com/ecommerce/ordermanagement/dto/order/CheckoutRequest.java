package com.ecommerce.ordermanagement.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CheckoutRequest(
        @NotNull @Valid ShippingAddressRequest shippingAddress
) {
}
