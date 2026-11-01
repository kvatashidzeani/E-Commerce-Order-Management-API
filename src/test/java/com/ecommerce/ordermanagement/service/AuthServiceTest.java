package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.domain.entity.Cart;
import com.ecommerce.ordermanagement.domain.entity.User;
import com.ecommerce.ordermanagement.domain.enums.Role;
import com.ecommerce.ordermanagement.dto.auth.AuthResponse;
import com.ecommerce.ordermanagement.dto.auth.LoginRequest;
import com.ecommerce.ordermanagement.dto.auth.RegisterRequest;
import com.ecommerce.ordermanagement.exception.BusinessException;
import com.ecommerce.ordermanagement.repository.CartRepository;
import com.ecommerce.ordermanagement.repository.UserRepository;
import com.ecommerce.ordermanagement.security.JwtService;
import com.ecommerce.ordermanagement.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesCustomerCartAndToken() {
        RegisterRequest request = new RegisterRequest(
                "new@test.com", "Password123!", "Nino", "Beridze"
        );
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.role()).isEqualTo(Role.CUSTOMER);
        assertThat(response.email()).isEqualTo("new@test.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("hashed");
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("dup@test.com", "Password123!", "A", "B")
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        User user = User.builder()
                .id(3L)
                .email("user@test.com")
                .passwordHash("hashed")
                .firstName("A")
                .lastName("B")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("login-token");

        AuthResponse response = authService.login(new LoginRequest("user@test.com", "Password123!"));

        assertThat(response.accessToken()).isEqualTo("login-token");
        assertThat(response.userId()).isEqualTo(3L);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void loginRejectsBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@test.com", "wrong")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid email or password");
    }
}
