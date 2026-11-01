package com.ecommerce.ordermanagement.integration;

import com.ecommerce.ordermanagement.domain.entity.Category;
import com.ecommerce.ordermanagement.domain.entity.Product;
import com.ecommerce.ordermanagement.domain.enums.OrderStatus;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    private Long productId;

    @BeforeEach
    void setUpProduct() {
        Category category = categoryRepository.findByNameIgnoreCase("Electronics")
                .orElseGet(() -> categoryRepository.save(Category.builder().name("Electronics").build()));

        Product product = productRepository.save(Product.builder()
                .category(category)
                .name("Integration Laptop")
                .description("test")
                .sku("INT-LAP-" + System.nanoTime())
                .price(new BigDecimal("250.00"))
                .stockQuantity(3)
                .active(true)
                .build());
        productId = product.getId();
    }

    @Test
    void checkoutPayAndCancelFlow() {
        String token = registerAndLogin("buyer_" + System.nanoTime() + "@test.com", "Password123!");

        addToCart(token, productId, 1);

        ResponseEntity<Map> checkout = authenticatedPost(token, "/api/orders", Map.of(
                "shippingAddress", Map.of(
                        "recipientName", "Buyer",
                        "phone", "+995555000000",
                        "country", "Georgia",
                        "city", "Tbilisi",
                        "street", "Street 1",
                        "postalCode", "0108"
                )
        ));
        assertThat(checkout.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(checkout.getBody()).isNotNull();
        assertThat(checkout.getBody().get("status")).isEqualTo(OrderStatus.PENDING.name());
        Number orderId = (Number) checkout.getBody().get("id");

        Product afterCheckout = productRepository.findById(productId).orElseThrow();
        assertThat(afterCheckout.getStockQuantity()).isEqualTo(2);

        ResponseEntity<Map> paid = authenticatedPost(token, "/api/orders/" + orderId.longValue() + "/pay", null);
        assertThat(paid.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(paid.getBody().get("status")).isEqualTo(OrderStatus.PAID.name());

        ResponseEntity<Map> cancelled = authenticatedPost(token, "/api/orders/" + orderId.longValue() + "/cancel", null);
        assertThat(cancelled.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(cancelled.getBody().get("status")).isEqualTo(OrderStatus.CANCELLED.name());

        Product afterCancel = productRepository.findById(productId).orElseThrow();
        assertThat(afterCancel.getStockQuantity()).isEqualTo(3);
    }

    private String registerAndLogin(String email, String password) {
        restTemplate.postForEntity("/api/auth/register", Map.of(
                "email", email,
                "password", password,
                "firstName", "Test",
                "lastName", "Buyer"
        ), Map.class);

        ResponseEntity<Map> login = restTemplate.postForEntity("/api/auth/login", Map.of(
                "email", email,
                "password", password
        ), Map.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) login.getBody().get("accessToken");
    }

    private void addToCart(String token, Long productId, int quantity) {
        ResponseEntity<Map> response = authenticatedPost(token, "/api/cart/items", Map.of(
                "productId", productId,
                "quantity", quantity
        ));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    private ResponseEntity<Map> authenticatedPost(String token, String path, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
    }
}
