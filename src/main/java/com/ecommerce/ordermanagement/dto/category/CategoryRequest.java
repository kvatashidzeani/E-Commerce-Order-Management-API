package com.ecommerce.ordermanagement.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 150) String name,
        Long parentId
) {
}
