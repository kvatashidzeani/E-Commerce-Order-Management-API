package com.ecommerce.ordermanagement.dto.auth;

import com.ecommerce.ordermanagement.domain.enums.Role;

public record AuthResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String email,
        String firstName,
        String lastName,
        Role role
) {
    public static AuthResponse bearer(
            String token,
            Long userId,
            String email,
            String firstName,
            String lastName,
            Role role
    ) {
        return new AuthResponse(token, "Bearer", userId, email, firstName, lastName, role);
    }
}
