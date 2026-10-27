package hu.finex.main.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.springframework.stereotype.Service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.exception.BusinessException;
import lombok.RequiredArgsConstructor;

// Devizaárfolyamok (1 egység = ennyi Ft): a forintos limitek átszámításához és a devizaváltáshoz

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final FinexProperties finexProperties;

    public Map<String, BigDecimal> rates() {
        return finexProperties.getExchangeRates();
    }

    // 1 egység ennyi forint
    public BigDecimal rate(String currency) {
        BigDecimal rate = finexProperties.getExchangeRates().get(currency);
        if (rate == null || rate.signum() <= 0) {
            throw new BusinessException("Ismeretlen devizanem: " + currency);
        }
        return rate;
    }

    // Az összeg forintban (a limitek ellenőrzéséhez nem kell kerekíteni)
    public BigDecimal toHuf(BigDecimal amount, String currency) {
        return amount.multiply(rate(currency));
    }

    // Egy forintösszeg az adott devizában, lefelé kerekítve (pl. egy limit ennél többet ne engedjen)
    public BigDecimal fromHuf(BigDecimal hufAmount, String currency) {
        return hufAmount.divide(rate(currency), 2, RoundingMode.DOWN);
    }

    // 1 egység "from" devizáért ennyi "to" deviza jár (tájékoztató, 6 tizedesre)
    public BigDecimal crossRate(String from, String to) {
        return rate(from).divide(rate(to), 6, RoundingMode.HALF_UP);
    }

    // Átváltás forinton keresztül, lefelé kerekítve: a bank sosem ír jóvá többet a terhelt értéknél
    public BigDecimal convert(BigDecimal amount, String from, String to) {
        if (from.equals(to)) {
            return amount;
        }
        return amount.multiply(rate(from)).divide(rate(to), 2, RoundingMode.DOWN);
    }
}
