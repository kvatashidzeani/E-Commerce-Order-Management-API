package com.ecommerce.ordermanagement.controller;

import com.ecommerce.ordermanagement.domain.enums.OrderStatus;
import com.ecommerce.ordermanagement.dto.order.CheckoutRequest;
import com.ecommerce.ordermanagement.dto.order.OrderResponse;
import com.ecommerce.ordermanagement.dto.order.OrderSummaryResponse;
import com.ecommerce.ordermanagement.dto.order.UpdateOrderStatusRequest;
import com.ecommerce.ordermanagement.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Checkout current cart into a new PENDING order")
    public OrderResponse checkout(@Valid @RequestBody CheckoutRequest request) {
        return orderService.checkout(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "List my orders")
    public Page<OrderSummaryResponse> myOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderService.getMyOrders(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my order by id")
    public OrderResponse getMyOrder(@PathVariable Long id) {
        return orderService.getMyOrder(id);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all orders (ADMIN), optional status filter")
    public Page<OrderSummaryResponse> adminOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return orderService.getAllOrders(status, pageable);
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get any order by id (ADMIN)")
    public OrderResponse getOrderForAdmin(@PathVariable Long id) {
        return orderService.getOrderForAdmin(id);
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Simulate payment (PENDING -> PAID)")
    public OrderResponse pay(@PathVariable Long id) {
        return orderService.pay(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancel order and restore stock")
    public OrderResponse cancel(@PathVariable Long id) {
        return orderService.cancel(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin status transition (e.g. PAID -> SHIPPED)")
    public OrderResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return orderService.updateStatus(id, request);
    }
}
