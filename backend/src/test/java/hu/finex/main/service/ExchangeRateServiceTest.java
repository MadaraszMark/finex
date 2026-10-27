package hu.finex.main.service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExchangeRateServiceTest {

    private ExchangeRateService service;

    @BeforeEach
    void setUp() {
        FinexProperties properties = new FinexProperties();
        properties.setExchangeRates(Map.of("HUF", BigDecimal.ONE, "EUR", new BigDecimal("395.00"), "USD", new BigDecimal("362.00")));
        service = new ExchangeRateService(properties);
    }

    @Test
    void toHuf_shouldMultiplyByRate() {
        assertEquals(0, new BigDecimal("3950.00").compareTo(service.toHuf(BigDecimal.TEN, "EUR")));
    }

    // 5 000 000 Ft / 395 = 12 658,227… €, a forintos limit devizában lefelé kerekítve
    @Test
    void fromHuf_shouldRoundDown() {
        assertEquals(new BigDecimal("12658.22"), service.fromHuf(new BigDecimal("5000000"), "EUR"));
    }

    // A bank sosem ír jóvá többet, mint a terhelt összeg értéke
    @Test
    void convert_shouldGoThroughHuf_andRoundDown() {
        assertEquals(new BigDecimal("253.16"), service.convert(new BigDecimal("100000"), "HUF", "EUR"));
        assertEquals(new BigDecimal("109.11"), service.convert(new BigDecimal("100"), "EUR", "USD"));
        assertEquals(new BigDecimal("5"), service.convert(new BigDecimal("5"), "HUF", "HUF"));
    }

    @Test
    void crossRate_shouldGiveSixDecimals() {
        assertEquals(new BigDecimal("395.000000"), service.crossRate("EUR", "HUF"));
    }

    @Test
    void rate_shouldThrowBusinessException_forUnknownCurrency() {
        assertThrows(BusinessException.class, () -> service.rate("GBP"));
    }
}
