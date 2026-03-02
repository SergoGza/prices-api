package com.sergio.prices.infrastructure.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PriceJpaRepository extends JpaRepository<PriceJpaEntity, Long> {

    @Query("""
            SELECT p FROM PriceJpaEntity p
            WHERE p.brandId = :brandId
              AND p.productId = :productId
              AND p.startDate <= :applicationDate
              AND p.endDate >= :applicationDate
            ORDER BY p.priority DESC
            LIMIT 1
            """)
    Optional<PriceJpaEntity> findApplicablePrice(
            @Param("brandId") long brandId,
            @Param("productId") long productId,
            @Param("applicationDate") LocalDateTime applicationDate);
}
