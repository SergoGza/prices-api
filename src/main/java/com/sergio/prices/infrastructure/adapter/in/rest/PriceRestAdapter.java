package com.sergio.prices.infrastructure.adapter.in.rest;

import com.sergio.prices.domain.port.in.FindApplicablePriceUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/prices")
public class PriceRestAdapter {

    private final FindApplicablePriceUseCase findApplicablePriceUseCase;
    private final PriceRestMapper priceRestMapper;

    public PriceRestAdapter(
            final FindApplicablePriceUseCase findApplicablePriceUseCase,
            final PriceRestMapper priceRestMapper) {
        this.findApplicablePriceUseCase = findApplicablePriceUseCase;
        this.priceRestMapper = priceRestMapper;
    }

    @GetMapping
    public PriceResponse findApplicablePrice(
            @RequestParam final long brandId,
            @RequestParam final long productId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime applicationDate) {
        return priceRestMapper.toResponse(
                findApplicablePriceUseCase.findApplicablePrice(brandId, productId, applicationDate));
    }
}
