package com.sergio.prices.infrastructure.adapter.out.persistence;

import com.sergio.prices.domain.model.Price;
import org.mapstruct.Mapper;

@Mapper
public interface PricePersistenceMapper {

    Price toDomain(PriceJpaEntity entity);
}
