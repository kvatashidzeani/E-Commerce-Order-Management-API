package com.ecommerce.ordermanagement.controller;

import com.ecommerce.ordermanagement.dto.product.ProductRequest;
import com.ecommerce.ordermanagement.dto.product.ProductResponse;
import com.ecommerce.ordermanagement.dto.product.ProductUpdateRequest;
import com.ecommerce.ordermanagement.service.ProductService;
import com.ecommerce.ordermanagement.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Products")
public class ProductController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "name", "price", "stockQuantity", "createdAt", "updatedAt"
    );

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List products with pagination, sorting and filters")
    public Page<ProductResponse> list(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        return productService.search(
                category,
                minPrice,
                maxPrice,
                search,
                includeInactive,
                toPageable(page, size, sort)
        );
    }

    private Pageable toPageable(int page, int size, String sort) {
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(property)) {
            throw new BusinessException(
                    "Invalid sort field '%s'. Allowed: %s".formatted(property, ALLOWED_SORT_FIELDS)
            );
        }
        Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by id")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create product (ADMIN)")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update product (ADMIN)")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Soft-delete product / deactivate (ADMIN)")
    public ProductResponse deactivate(@PathVariable Long id) {
        return productService.deactivate(id);
    }
}
