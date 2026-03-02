package com.sergio.prices.domain.port.out;

import com.sergio.prices.domain.model.Price;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PriceRepositoryPort {

    Optional<Price> findApplicablePrice(long brandId, long productId, LocalDateTime applicationDate);
}
