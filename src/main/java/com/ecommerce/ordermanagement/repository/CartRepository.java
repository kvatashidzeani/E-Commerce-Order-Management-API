package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.domain.entity.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.category"})
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Optional<Cart> findWithItemsByUserId(@Param("userId") Long userId);
}
