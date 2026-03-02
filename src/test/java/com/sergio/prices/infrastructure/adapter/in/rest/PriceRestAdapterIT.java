package com.sergio.prices.infrastructure.adapter.in.rest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class PriceRestAdapterIT {

  private record PriceResult(int priceList, BigDecimal price) {}

  @LocalServerPort private int port;

  private final RestTemplate restTemplate = new RestTemplate();

  private static final long BRAND_ID = 1L;
  private static final long PRODUCT_ID = 35455L;
  private static final String URL_TEMPLATE =
      "http://localhost:%d/api/prices?brandId=%d&productId=%d&applicationDate=%s";

  private static Stream<Arguments> priceScenarios() {
    return Stream.of(
        Arguments.of(
            "base price applies when no promotion is active",
            "2020-06-14T10:00:00",
            1,
            new BigDecimal("35.50")),
        Arguments.of(
            "higher-priority promo takes over during afternoon window on day 1",
            "2020-06-14T16:00:00",
            2,
            new BigDecimal("25.45")),
        Arguments.of(
            "base price resumes after afternoon promo window closes on day 1",
            "2020-06-14T21:00:00",
            1,
            new BigDecimal("35.50")),
        Arguments.of(
            "higher-priority promo applies during morning window on day 2",
            "2020-06-15T10:00:00",
            3,
            new BigDecimal("30.50")),
        Arguments.of(
            "permanent higher-price list takes over from day 3 onward",
            "2020-06-16T21:00:00",
            4,
            new BigDecimal("38.95")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("priceScenarios")
  void findApplicablePrice(
      final String scenario,
      final String applicationDate,
      final int expectedPriceList,
      final BigDecimal expectedPrice) {
    final String url = String.format(URL_TEMPLATE, port, BRAND_ID, PRODUCT_ID, applicationDate);

    final ResponseEntity<PriceResult> response = restTemplate.getForEntity(url, PriceResult.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().priceList()).isEqualTo(expectedPriceList);
    assertThat(response.getBody().price()).isEqualByComparingTo(expectedPrice);
  }
}
