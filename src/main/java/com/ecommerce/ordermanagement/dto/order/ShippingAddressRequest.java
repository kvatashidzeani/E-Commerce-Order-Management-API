package com.ecommerce.ordermanagement.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShippingAddressRequest(
        @NotBlank @Size(max = 200) String recipientName,
        @NotBlank @Size(max = 50) String phone,
        @NotBlank @Size(max = 100) String country,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 255) String street,
        @NotBlank @Size(max = 30) String postalCode
) {
}
