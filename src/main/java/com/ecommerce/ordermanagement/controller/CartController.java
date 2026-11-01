package com.ecommerce.ordermanagement.controller;

import com.ecommerce.ordermanagement.dto.cart.AddCartItemRequest;
import com.ecommerce.ordermanagement.dto.cart.CartResponse;
import com.ecommerce.ordermanagement.dto.cart.UpdateCartItemRequest;
import com.ecommerce.ordermanagement.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current customer's cart")
    public CartResponse getCart() {
        return cartService.getMyCart();
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add product to cart")
    public CartResponse addItem(@Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(request);
    }

    @PatchMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public CartResponse updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateItem(itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public CartResponse removeItem(@PathVariable Long itemId) {
        return cartService.removeItem(itemId);
    }
}
