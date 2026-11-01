package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.domain.entity.Cart;
import com.ecommerce.ordermanagement.domain.entity.CartItem;
import com.ecommerce.ordermanagement.domain.entity.Category;
import com.ecommerce.ordermanagement.domain.entity.Product;
import com.ecommerce.ordermanagement.domain.entity.User;
import com.ecommerce.ordermanagement.domain.enums.Role;
import com.ecommerce.ordermanagement.dto.cart.AddCartItemRequest;
import com.ecommerce.ordermanagement.dto.cart.CartResponse;
import com.ecommerce.ordermanagement.exception.BusinessException;
import com.ecommerce.ordermanagement.repository.CartRepository;
import com.ecommerce.ordermanagement.repository.ProductRepository;
import com.ecommerce.ordermanagement.repository.UserRepository;
import com.ecommerce.ordermanagement.support.SecurityTestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        SecurityTestUtils.authenticate(1L, "customer@test.com", Role.CUSTOMER);

        Category category = Category.builder().id(1L).name("Electronics").build();
        product = Product.builder()
                .id(10L)
                .name("Laptop")
                .sku("LAP-1")
                .price(new BigDecimal("100.00"))
                .stockQuantity(5)
                .active(true)
                .category(category)
                .version(0L)
                .build();

        User user = User.builder().id(1L).email("customer@test.com").role(Role.CUSTOMER).build();
        cart = Cart.builder().id(100L).user(user).items(new ArrayList<>()).build();
    }

    @AfterEach
    void tearDown() {
        SecurityTestUtils.clear();
    }

    @Test
    void addItemCalculatesLineTotalAndCartTotal() {
        when(cartRepository.findWithItemsByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findWithCategoryById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.addItem(new AddCartItemRequest(10L, 2));

        assertThat(response.totalItems()).isEqualTo(2);
        assertThat(response.totalAmount()).isEqualByComparingTo("200.00");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().lineTotal()).isEqualByComparingTo("200.00");
    }

    @Test
    void addItemRejectsQuantityAboveStock() {
        when(cartRepository.findWithItemsByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findWithCategoryById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(new AddCartItemRequest(10L, 6)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void addItemIncreasesExistingLineQuantity() {
        CartItem existing = CartItem.builder().id(1L).cart(cart).product(product).quantity(1).build();
        cart.getItems().add(existing);

        when(cartRepository.findWithItemsByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findWithCategoryById(10L)).thenReturn(Optional.of(product));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartResponse response = cartService.addItem(new AddCartItemRequest(10L, 2));

        ArgumentCaptor<Cart> captor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(captor.capture());
        assertThat(captor.getValue().getItems()).hasSize(1);
        assertThat(captor.getValue().getItems().getFirst().getQuantity()).isEqualTo(3);
        assertThat(response.totalAmount()).isEqualByComparingTo("300.00");
    }
}
