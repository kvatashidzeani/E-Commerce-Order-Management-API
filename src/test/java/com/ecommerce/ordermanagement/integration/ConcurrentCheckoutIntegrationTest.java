package com.ecommerce.ordermanagement.integration;

import com.ecommerce.ordermanagement.domain.entity.Category;
import com.ecommerce.ordermanagement.domain.entity.Product;
import com.ecommerce.ordermanagement.repository.CategoryRepository;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import com.ecommerce.ordermanagement.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ConcurrentCheckoutIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void setUpProductWithOneStock() {
        Category category = categoryRepository.findByNameIgnoreCase("Electronics")
                .orElseGet(() -> categoryRepository.save(Category.builder().name("Electronics").build()));

        Product product = productRepository.save(Product.builder()
                .category(category)
                .name("Last Unit Phone")
                .description("concurrency")
                .sku("CONC-" + System.nanoTime())
                .price(new BigDecimal("99.00"))
                .stockQuantity(1)
                .active(true)
                .build());
        productId = product.getId();
    }

    @Test
    void onlyOneOfTwoParallelCheckoutsSucceeds() throws Exception {
        String tokenA = registerAndLogin("conc_a_" + System.nanoTime() + "@test.com");
        String tokenB = registerAndLogin("conc_b_" + System.nanoTime() + "@test.com");

        addToCart(tokenA, productId, 1);
        addToCart(tokenB, productId, 1);

        Map<String, Object> checkoutBody = Map.of(
                "shippingAddress", Map.of(
                        "recipientName", "Buyer",
                        "phone", "+995555000000",
                        "country", "Georgia",
                        "city", "Tbilisi",
                        "street", "Street 1",
                        "postalCode", "0108"
                )
        );

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Integer>> tasks = List.of(
                    () -> checkout(tokenA, checkoutBody).getStatusCode().value(),
                    () -> checkout(tokenB, checkoutBody).getStatusCode().value()
            );

            List<Future<Integer>> futures = pool.invokeAll(tasks);
            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> future : futures) {
                statuses.add(future.get(30, TimeUnit.SECONDS));
            }

            long successCount = statuses.stream().filter(code -> code == HttpStatus.CREATED.value()).count();
            long conflictOrBad = statuses.stream()
                    .filter(code -> code >= 400)
                    .count();

            assertThat(successCount).isEqualTo(1);
            assertThat(conflictOrBad).isEqualTo(1);

            Product product = productRepository.findById(productId).orElseThrow();
            assertThat(product.getStockQuantity()).isZero();
        } finally {
            pool.shutdownNow();
        }
    }

    private String registerAndLogin(String email) {
        restTemplate.postForEntity("/api/auth/register", Map.of(
                "email", email,
                "password", "Password123!",
                "firstName", "Conc",
                "lastName", "User"
        ), Map.class);

        ResponseEntity<Map> login = restTemplate.postForEntity("/api/auth/login", Map.of(
                "email", email,
                "password", "Password123!"
        ), Map.class);
        return (String) login.getBody().get("accessToken");
    }

    private void addToCart(String token, Long productId, int quantity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.exchange(
                "/api/cart/items",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("productId", productId, "quantity", quantity), headers),
                Map.class
        );
    }

    private ResponseEntity<Map> checkout(String token, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange("/api/orders", HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
    }
}
