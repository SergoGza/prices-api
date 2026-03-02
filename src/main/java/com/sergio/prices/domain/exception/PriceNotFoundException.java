package com.sergio.prices.domain.exception;

import java.time.LocalDateTime;

public class PriceNotFoundException extends RuntimeException {

    private static final String NOT_FOUND_TEMPLATE =
            "No price found for brandId=%d, productId=%d, applicationDate=%s";

    public PriceNotFoundException(final long brandId, final long productId, final LocalDateTime applicationDate) {
        super(String.format(NOT_FOUND_TEMPLATE, brandId, productId, applicationDate));
    }
}
