package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.domain.entity.Cart;
import com.ecommerce.ordermanagement.domain.entity.CartItem;
import com.ecommerce.ordermanagement.domain.entity.Order;
import com.ecommerce.ordermanagement.domain.entity.OrderItem;
import com.ecommerce.ordermanagement.domain.entity.OrderStatusHistory;
import com.ecommerce.ordermanagement.domain.entity.Product;
import com.ecommerce.ordermanagement.domain.entity.User;
import com.ecommerce.ordermanagement.domain.enums.OrderStatus;
import com.ecommerce.ordermanagement.domain.enums.Role;
import com.ecommerce.ordermanagement.dto.order.CheckoutRequest;
import com.ecommerce.ordermanagement.dto.order.OrderItemResponse;
import com.ecommerce.ordermanagement.dto.order.OrderResponse;
import com.ecommerce.ordermanagement.dto.order.OrderStatusHistoryResponse;
import com.ecommerce.ordermanagement.dto.order.OrderSummaryResponse;
import com.ecommerce.ordermanagement.dto.order.ShippingAddressRequest;
import com.ecommerce.ordermanagement.dto.order.UpdateOrderStatusRequest;
import com.ecommerce.ordermanagement.exception.BusinessException;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.repository.CartRepository;
import com.ecommerce.ordermanagement.repository.OrderRepository;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import com.ecommerce.ordermanagement.repository.UserRepository;
import com.ecommerce.ordermanagement.security.SecurityUtils;
import com.ecommerce.ordermanagement.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        Long userId = SecurityUtils.currentUserId();
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseThrow(() -> new BusinessException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BusinessException("Cart is empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        ShippingAddressRequest shipping = request.shippingAddress();
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .status(OrderStatus.PENDING)
                .shippingRecipient(shipping.recipientName())
                .shippingPhone(shipping.phone())
                .shippingCountry(shipping.country())
                .shippingCity(shipping.city())
                .shippingStreet(shipping.street())
                .shippingPostalCode(shipping.postalCode())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Product product = productRepository.findByIdForUpdate(cartItem.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + cartItem.getProduct().getId()));

            if (!product.isActive()) {
                throw new BusinessException("Product is not available: " + product.getName());
            }
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BusinessException(
                        "Insufficient stock for '%s'. Available: %d, requested: %d"
                                .formatted(product.getName(), product.getStockQuantity(), cartItem.getQuantity()),
                        HttpStatus.CONFLICT
                );
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(lineTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productNameSnapshot(product.getName())
                    .unitPriceAtPurchase(unitPrice)
                    .quantity(cartItem.getQuantity())
                    .lineTotal(lineTotal)
                    .build();
            order.getItems().add(orderItem);
        }

        order.setTotalAmount(total);
        appendHistory(order, null, OrderStatus.PENDING, user, "Order created from checkout");

        cart.getItems().clear();
        cartRepository.save(cart);

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    public Page<OrderSummaryResponse> getMyOrders(Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(SecurityUtils.currentUserId(), pageable)
                .map(this::toSummary);
    }

    public Page<OrderSummaryResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page = status == null
                ? orderRepository.findAllByOrderByCreatedAtDesc(pageable)
                : orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return page.map(this::toSummary);
    }

    private OrderSummaryResponse toSummary(Order order) {
        return new OrderSummaryResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getItems() == null ? 0 : order.getItems().size(),
                order.getCreatedAt()
        );
    }

    public OrderResponse getMyOrder(Long orderId) {
        Order order = orderRepository.findDetailedByIdAndUserId(orderId, SecurityUtils.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return toResponse(order);
    }

    public OrderResponse getOrderForAdmin(Long orderId) {
        Order order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return toResponse(order);
    }

    @Transactional
    public OrderResponse pay(Long orderId) {
        Order order = getOwnedOrderForUpdate(orderId);
        transition(order, OrderStatus.PAID, SecurityUtils.currentUser(), "Payment simulated");
        order.setPaidAt(Instant.now());
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancel(Long orderId) {
        Order order = getOwnedOrderForUpdate(orderId);
        transition(order, OrderStatus.CANCELLED, SecurityUtils.currentUser(), "Cancelled by customer");
        order.setCancelledAt(Instant.now());
        restoreStock(order);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        UserPrincipal actor = SecurityUtils.currentUser();
        if (actor.getRole() != Role.ADMIN) {
            throw new BusinessException("Only admins can update order status", HttpStatus.FORBIDDEN);
        }

        Order order = orderRepository.findDetailedById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        OrderStatus target = request.status();
        transition(order, target, actor, request.comment() == null ? "Status updated by admin" : request.comment());

        Instant now = Instant.now();
        switch (target) {
            case PAID -> order.setPaidAt(now);
            case SHIPPED -> order.setShippedAt(now);
            case DELIVERED -> order.setDeliveredAt(now);
            case CANCELLED -> {
                order.setCancelledAt(now);
                restoreStock(order);
            }
            default -> {
            }
        }

        return toResponse(orderRepository.save(order));
    }

    private Order getOwnedOrderForUpdate(Long orderId) {
        return orderRepository.findDetailedByIdAndUserId(orderId, SecurityUtils.currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private void transition(Order order, OrderStatus target, UserPrincipal actor, String comment) {
        OrderStatus current = order.getStatus();
        if (!current.canTransitionTo(target)) {
            throw new BusinessException(
                    "Illegal status transition: %s -> %s".formatted(current, target),
                    HttpStatus.CONFLICT
            );
        }

        User changedBy = userRepository.findById(actor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + actor.getId()));

        appendHistory(order, current, target, changedBy, comment);
        order.setStatus(target);
    }

    private void appendHistory(
            Order order,
            OrderStatus from,
            OrderStatus to,
            User changedBy,
            String comment
    ) {
        order.getStatusHistory().add(OrderStatusHistory.builder()
                .order(order)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .changedAt(Instant.now())
                .comment(comment)
                .build());
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            productRepository.incrementStock(item.getProduct().getId(), item.getQuantity());
        }
    }

    private String generateOrderNumber() {
        String candidate;
        do {
            candidate = "ORD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (orderRepository.existsByOrderNumber(candidate));
        return candidate;
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingRecipient(),
                order.getShippingPhone(),
                order.getShippingCountry(),
                order.getShippingCity(),
                order.getShippingStreet(),
                order.getShippingPostalCode(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getProduct().getId(),
                                item.getProductNameSnapshot(),
                                item.getUnitPriceAtPurchase(),
                                item.getQuantity(),
                                item.getLineTotal()
                        ))
                        .toList(),
                order.getStatusHistory().stream()
                        .sorted(Comparator.comparing(OrderStatusHistory::getChangedAt))
                        .map(history -> new OrderStatusHistoryResponse(
                                history.getFromStatus(),
                                history.getToStatus(),
                                history.getChangedBy() == null ? null : history.getChangedBy().getId(),
                                history.getChangedAt(),
                                history.getComment()
                        ))
                        .toList(),
                order.getPaidAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                order.getCancelledAt(),
                order.getCreatedAt()
        );
    }
}
