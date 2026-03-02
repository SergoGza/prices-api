package com.sergio.prices.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Price(
        long brandId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int priceList,
        long productId,
        int priority,
        BigDecimal price,
        String currency) {}
