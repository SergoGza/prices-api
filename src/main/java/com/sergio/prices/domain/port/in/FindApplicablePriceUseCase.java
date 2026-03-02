package com.sergio.prices.domain.port.in;

import com.sergio.prices.domain.model.Price;
import java.time.LocalDateTime;

public interface FindApplicablePriceUseCase {

    Price findApplicablePrice(long brandId, long productId, LocalDateTime applicationDate);
}
