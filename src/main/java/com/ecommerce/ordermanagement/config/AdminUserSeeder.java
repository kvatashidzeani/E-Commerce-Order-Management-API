package com.ecommerce.ordermanagement.config;

import com.ecommerce.ordermanagement.domain.entity.User;
import com.ecommerce.ordermanagement.domain.enums.Role;
import com.ecommerce.ordermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
@Order(1)
@RequiredArgsConstructor
public class AdminUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUser("admin@shop.com", "Admin123!", "System", "Admin", Role.ADMIN);
        seedUser("user@shop.com", "User123!", "Demo", "Customer", Role.CUSTOMER);
        // Keep previous local admin email working if already present in older DBs
        seedUser("admin@ecommerce.local", "Admin123!", "System", "Admin", Role.ADMIN);
    }

    private void seedUser(String email, String rawPassword, String firstName, String lastName, Role role) {
        if (userRepository.existsByEmail(email)) {
            return;
        }

        userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .enabled(true)
                .build());

        log.info("Seeded {} user: {} / {}", role, email, rawPassword);
    }
}
