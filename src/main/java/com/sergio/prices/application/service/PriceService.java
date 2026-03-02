package com.sergio.prices.application.service;

import com.sergio.prices.domain.exception.PriceNotFoundException;
import com.sergio.prices.domain.model.Price;
import com.sergio.prices.domain.port.in.FindApplicablePriceUseCase;
import com.sergio.prices.domain.port.out.PriceRepositoryPort;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PriceService implements FindApplicablePriceUseCase {

    private final PriceRepositoryPort priceRepositoryPort;

    public PriceService(final PriceRepositoryPort priceRepositoryPort) {
        this.priceRepositoryPort = priceRepositoryPort;
    }

    @Override
    public Price findApplicablePrice(final long brandId, final long productId, final LocalDateTime applicationDate) {
        return priceRepositoryPort.findApplicablePrice(brandId, productId, applicationDate)
                .orElseThrow(() -> new PriceNotFoundException(brandId, productId, applicationDate));
    }
}
