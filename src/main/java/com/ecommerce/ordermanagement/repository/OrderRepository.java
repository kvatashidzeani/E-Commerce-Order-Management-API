package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.domain.entity.Order;
import com.ecommerce.ordermanagement.domain.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Only one bag ({@code items}) is fetched here on purpose.
     * Fetching {@code items} and {@code statusHistory} together causes
     * Hibernate MultipleBagFetchException.
     */
    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("SELECT o FROM Order o WHERE o.id = :id AND o.user.id = :userId")
    Optional<Order> findDetailedByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @EntityGraph(attributePaths = "items")
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    boolean existsByOrderNumber(String orderNumber);
}
