package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.domain.entity.Order;
import com.ecommerce.ordermanagement.domain.entity.OrderItem;
import com.ecommerce.ordermanagement.domain.entity.Product;
import com.ecommerce.ordermanagement.domain.entity.User;
import com.ecommerce.ordermanagement.domain.enums.OrderStatus;
import com.ecommerce.ordermanagement.domain.enums.Role;
import com.ecommerce.ordermanagement.dto.order.UpdateOrderStatusRequest;
import com.ecommerce.ordermanagement.exception.BusinessException;
import com.ecommerce.ordermanagement.repository.CartRepository;
import com.ecommerce.ordermanagement.repository.OrderRepository;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import com.ecommerce.ordermanagement.repository.UserRepository;
import com.ecommerce.ordermanagement.support.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private User customer;
    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(1L)
                .email("customer@test.com")
                .passwordHash("hash")
                .firstName("A")
                .lastName("B")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();

        product = Product.builder()
                .id(10L)
                .name("Phone")
                .sku("PH-1")
                .price(new BigDecimal("50.00"))
                .stockQuantity(1)
                .active(true)
                .version(0L)
                .build();

        order = Order.builder()
                .id(2L)
                .orderNumber("ORD-1")
                .user(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .shippingRecipient("A")
                .shippingPhone("1")
                .shippingCountry("GE")
                .shippingCity("Tbilisi")
                .shippingStreet("Street")
                .shippingPostalCode("0100")
                .items(new ArrayList<>())
                .statusHistory(new ArrayList<>())
                .build();

        order.getItems().add(OrderItem.builder()
                .id(1L)
                .order(order)
                .product(product)
                .productNameSnapshot("Phone")
                .unitPriceAtPurchase(new BigDecimal("50.00"))
                .quantity(1)
                .lineTotal(new BigDecimal("50.00"))
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void payTransitionsPendingToPaid() {
        SecurityTestUtils.authenticate(1L, "customer@test.com", Role.CUSTOMER);
        when(orderRepository.findDetailedByIdAndUserId(2L, 1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.pay(2L);

        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
        assertThat(response.paidAt()).isNotNull();
        assertThat(response.statusHistory()).isNotEmpty();
    }

    @Test
    void payRejectsIllegalTransitionFromShipped() {
        SecurityTestUtils.authenticate(1L, "customer@test.com", Role.CUSTOMER);
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findDetailedByIdAndUserId(2L, 1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.pay(2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Illegal status transition");
    }

    @Test
    void cancelRestoresStock() {
        SecurityTestUtils.authenticate(1L, "customer@test.com", Role.CUSTOMER);
        when(orderRepository.findDetailedByIdAndUserId(2L, 1L)).thenReturn(Optional.of(order));
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.incrementStock(10L, 1)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.cancel(2L);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(productRepository).incrementStock(10L, 1);
    }

    @Test
    void adminCanShipPaidOrder() {
        SecurityTestUtils.authenticate(99L, "admin@test.com", Role.ADMIN);
        order.setStatus(OrderStatus.PAID);
        User admin = User.builder().id(99L).email("admin@test.com").role(Role.ADMIN)
                .passwordHash("h").firstName("Ad").lastName("Min").enabled(true).build();

        when(orderRepository.findDetailedById(2L)).thenReturn(Optional.of(order));
        when(userRepository.findById(99L)).thenReturn(Optional.of(admin));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.updateStatus(2L, new UpdateOrderStatusRequest(OrderStatus.SHIPPED, "shipped"));

        assertThat(response.status()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(response.shippedAt()).isNotNull();
        verify(productRepository, never()).incrementStock(anyLong(), anyInt());
    }
}
