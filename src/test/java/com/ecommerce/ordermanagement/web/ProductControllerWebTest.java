package com.ecommerce.ordermanagement.web;

import com.ecommerce.ordermanagement.controller.ProductController;
import com.ecommerce.ordermanagement.dto.product.ProductResponse;
import com.ecommerce.ordermanagement.security.JwtAuthenticationFilter;
import com.ecommerce.ordermanagement.security.JwtService;
import com.ecommerce.ordermanagement.security.SecurityConfig;
import com.ecommerce.ordermanagement.service.CustomUserDetailsService;
import com.ecommerce.ordermanagement.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class})
@TestPropertySource(properties = {
        "app.jwt.secret=TestSecretKeyForJwtThatIsLongEnoughForHs256Algo!!",
        "app.jwt.expiration-ms=3600000"
})
class ProductControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void listProductsIsPublic() throws Exception {
        ProductResponse product = new ProductResponse(
                1L, "Laptop", "desc", null, "SKU-1", new BigDecimal("10.00"),
                5, 1L, "Electronics", true, 0L, Instant.now(), Instant.now()
        );
        when(productService.search(isNull(), isNull(), isNull(), isNull(), anyBoolean(), any()))
                .thenReturn(new PageImpl<>(List.of(product), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/products").param("page", "0").param("size", "20").param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"));
    }

    @Test
    void createProductRequiresAdmin() throws Exception {
        String body = """
                {
                  "name": "Laptop",
                  "description": "desc",
                  "sku": "SKU-2",
                  "price": 10.00,
                  "stockQuantity": 1,
                  "categoryId": 1,
                  "active": true
                }
                """;

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .with(user("customer").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProductValidatesPayload() throws Exception {
        String invalidBody = """
                {
                  "name": "",
                  "sku": "",
                  "price": -1,
                  "stockQuantity": -5,
                  "categoryId": null
                }
                """;

        mockMvc.perform(post("/api/products")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }
}
