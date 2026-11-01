package com.ecommerce.ordermanagement.support;

import com.ecommerce.ordermanagement.domain.entity.User;
import com.ecommerce.ordermanagement.domain.enums.Role;
import com.ecommerce.ordermanagement.security.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityTestUtils {

    private SecurityTestUtils() {
    }

    public static void authenticate(Long userId, String email, Role role) {
        User user = User.builder()
                .id(userId)
                .email(email)
                .passwordHash("hash")
                .firstName("Test")
                .lastName("User")
                .role(role)
                .enabled(true)
                .build();
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
