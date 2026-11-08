package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.domain.entity.Category;
import com.ecommerce.ordermanagement.domain.entity.Product;
import com.ecommerce.ordermanagement.dto.product.ProductRequest;
import com.ecommerce.ordermanagement.dto.product.ProductResponse;
import com.ecommerce.ordermanagement.dto.product.ProductUpdateRequest;
import com.ecommerce.ordermanagement.exception.BusinessException;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.mapper.ProductMapper;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryService categoryService;
    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    void createProductPersistsMappedEntity() {
        ProductRequest request = new ProductRequest(
                "Phone", "desc", null, "SKU-9", new BigDecimal("20.00"), 4, 1L, true
        );
        Category category = Category.builder().id(1L).name("Phones").build();
        Product entity = Product.builder().name("Phone").sku("SKU-9").build();
        Product saved = Product.builder().id(11L).name("Phone").sku("SKU-9").category(category).build();
        ProductResponse response = new ProductResponse(
                11L, "Phone", "desc", null, "SKU-9", new BigDecimal("20.00"),
                4, 1L, "Phones", true, 0L, null, null
        );

        when(productRepository.existsBySkuIgnoreCase("SKU-9")).thenReturn(false);
        when(productMapper.toEntity(request)).thenReturn(entity);
        when(categoryService.getCategory(1L)).thenReturn(category);
        when(productRepository.save(entity)).thenReturn(saved);
        when(productMapper.toResponse(saved)).thenReturn(response);

        ProductResponse result = productService.create(request);

        assertThat(result.id()).isEqualTo(11L);
        assertThat(entity.getCategory()).isEqualTo(category);
    }

    @Test
    void createRejectsDuplicateSku() {
        when(productRepository.existsBySkuIgnoreCase("SKU-9")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(
                new ProductRequest("Phone", "d", null, "SKU-9", BigDecimal.ONE, 1, 1L, true)
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("SKU already exists");
    }

    @Test
    void deactivateSetsActiveFalse() {
        Category category = Category.builder().id(1L).name("Phones").build();
        Product product = Product.builder()
                .id(5L)
                .name("Phone")
                .sku("SKU-1")
                .active(true)
                .category(category)
                .build();
        when(productRepository.findWithCategoryById(5L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toResponse(product)).thenReturn(new ProductResponse(
                5L, "Phone", null, null, "SKU-1", BigDecimal.ONE, 1, 1L, "Phones",
                false, 0L, null, null
        ));

        ProductResponse response = productService.deactivate(5L);

        assertThat(product.isActive()).isFalse();
        assertThat(response.active()).isFalse();
    }

    @Test
    void updateFailsWhenMissing() {
        when(productRepository.findWithCategoryById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(
                99L, new ProductUpdateRequest("x", null, null, null, null, null, null)
        )).isInstanceOf(ResourceNotFoundException.class);
    }
}
