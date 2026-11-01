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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new BusinessException("Email already registered: " + request.email(), HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .email(request.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);

        cartRepository.save(Cart.builder().user(saved).build());

        UserPrincipal principal = new UserPrincipal(saved);
        String token = jwtService.generateToken(principal);
        return AuthResponse.bearer(
                token,
                saved.getId(),
                saved.getEmail(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getRole()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email().trim().toLowerCase(),
                            request.password()
                    )
            );
        } catch (AuthenticationException ex) {
            throw new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(request.email().trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);
        return AuthResponse.bearer(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}
