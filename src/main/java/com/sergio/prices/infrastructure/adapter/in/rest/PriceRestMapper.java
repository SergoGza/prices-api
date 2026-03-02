package com.sergio.prices.infrastructure.adapter.in.rest;

import com.sergio.prices.domain.model.Price;
import org.mapstruct.Mapper;

@Mapper
public interface PriceRestMapper {

    PriceResponse toResponse(Price price);
}
