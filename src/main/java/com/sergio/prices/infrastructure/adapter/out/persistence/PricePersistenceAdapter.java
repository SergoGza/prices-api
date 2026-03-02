package com.sergio.prices.infrastructure.adapter.out.persistence;

import com.sergio.prices.domain.model.Price;
import com.sergio.prices.domain.port.out.PriceRepositoryPort;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class PricePersistenceAdapter implements PriceRepositoryPort {

    private final PriceJpaRepository jpaRepository;
    private final PricePersistenceMapper mapper;

    public PricePersistenceAdapter(
            final PriceJpaRepository jpaRepository, final PricePersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Price> findApplicablePrice(
            final long brandId, final long productId, final LocalDateTime applicationDate) {
        return jpaRepository
                .findApplicablePrice(brandId, productId, applicationDate)
                .map(mapper::toDomain);
    }
}
