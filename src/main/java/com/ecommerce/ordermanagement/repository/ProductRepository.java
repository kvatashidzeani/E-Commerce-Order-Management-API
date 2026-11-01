package com.ecommerce.ordermanagement.repository;

import com.ecommerce.ordermanagement.domain.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    @EntityGraph(attributePaths = "category")
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findWithCategoryById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Product p
            SET p.stockQuantity = p.stockQuantity - :quantity
            WHERE p.id = :productId
              AND p.active = true
              AND p.stockQuantity >= :quantity
            """)
    int decrementStockIfAvailable(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Product p
            SET p.stockQuantity = p.stockQuantity + :quantity
            WHERE p.id = :productId
            """)
    int incrementStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    @EntityGraph(attributePaths = "category")
    @Query("""
            SELECT p FROM Product p
            WHERE (:activeOnly = false OR p.active = true)
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
              AND (:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            """)
    Page<Product> search(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("search") String search,
            @Param("activeOnly") boolean activeOnly,
            Pageable pageable
    );
}
