package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.domain.entity.Product;
import com.ecommerce.ordermanagement.dto.product.ProductRequest;
import com.ecommerce.ordermanagement.dto.product.ProductResponse;
import com.ecommerce.ordermanagement.dto.product.ProductUpdateRequest;
import com.ecommerce.ordermanagement.exception.BusinessException;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.mapper.ProductMapper;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;

    public Page<ProductResponse> search(
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String search,
            boolean includeInactive,
            Pageable pageable
    ) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        return productRepository.search(
                        categoryId,
                        minPrice,
                        maxPrice,
                        normalizedSearch,
                        !includeInactive,
                        pageable
                )
                .map(productMapper::toResponse);
    }

    public ProductResponse findById(Long id) {
        return productMapper.toResponse(getProductWithCategory(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new BusinessException("SKU already exists: " + request.sku(), HttpStatus.CONFLICT);
        }

        Product product = productMapper.toEntity(request);
        product.setCategory(categoryService.getCategory(request.categoryId()));
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = getProductWithCategory(id);

        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl().isBlank() ? null : request.imageUrl().trim());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.stockQuantity() != null) {
            product.setStockQuantity(request.stockQuantity());
        }
        if (request.categoryId() != null) {
            product.setCategory(categoryService.getCategory(request.categoryId()));
        }
        if (request.active() != null) {
            product.setActive(request.active());
        }

        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse deactivate(Long id) {
        Product product = getProductWithCategory(id);
        product.setActive(false);
        return productMapper.toResponse(productRepository.save(product));
    }

    public Product getProductWithCategory(Long id) {
        return productRepository.findWithCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }
}
