package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.domain.entity.Cart;
import com.ecommerce.ordermanagement.domain.entity.CartItem;
import com.ecommerce.ordermanagement.domain.entity.Product;
import com.ecommerce.ordermanagement.domain.entity.User;
import com.ecommerce.ordermanagement.dto.cart.AddCartItemRequest;
import com.ecommerce.ordermanagement.dto.cart.CartItemResponse;
import com.ecommerce.ordermanagement.dto.cart.CartResponse;
import com.ecommerce.ordermanagement.dto.cart.UpdateCartItemRequest;
import com.ecommerce.ordermanagement.exception.BusinessException;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.repository.CartRepository;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import com.ecommerce.ordermanagement.repository.UserRepository;
import com.ecommerce.ordermanagement.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartResponse getMyCart() {
        return toResponse(getOrCreateCartWithItems(SecurityUtils.currentUserId()));
    }

    @Transactional
    public CartResponse addItem(AddCartItemRequest request) {
        Cart cart = getOrCreateCartWithItems(SecurityUtils.currentUserId());
        Product product = getActiveProduct(request.productId());

        CartItem existing = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        int newQuantity = request.quantity() + (existing == null ? 0 : existing.getQuantity());
        validateStock(product, newQuantity);

        if (existing == null) {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .build();
            cart.getItems().add(item);
        } else {
            existing.setQuantity(newQuantity);
        }

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCartWithItems(SecurityUtils.currentUserId());
        CartItem item = findItem(cart, itemId);
        validateStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(Long itemId) {
        Cart cart = getOrCreateCartWithItems(SecurityUtils.currentUserId());
        CartItem item = findItem(cart, itemId);
        cart.getItems().remove(item);
        return toResponse(cartRepository.save(cart));
    }

    private Cart getOrCreateCartWithItems(Long userId) {
        return cartRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                    Cart cart = Cart.builder().user(user).items(new ArrayList<>()).build();
                    return cartRepository.save(cart);
                });
    }

    private Product getActiveProduct(Long productId) {
        Product product = productRepository.findWithCategoryById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        if (!product.isActive()) {
            throw new BusinessException("Product is not available: " + productId, HttpStatus.BAD_REQUEST);
        }
        return product;
    }

    private void validateStock(Product product, int quantity) {
        if (quantity > product.getStockQuantity()) {
            throw new BusinessException(
                    "Insufficient stock for product '%s'. Available: %d, requested: %d"
                            .formatted(product.getName(), product.getStockQuantity(), quantity),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private CartItem findItem(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = items.stream()
                .mapToInt(CartItemResponse::quantity)
                .sum();

        return new CartResponse(cart.getId(), items, total, totalItems);
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Product product = item.getProduct();
        BigDecimal unitPrice = product.getPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSku(),
                unitPrice,
                item.getQuantity(),
                lineTotal,
                product.getStockQuantity()
        );
    }
}
