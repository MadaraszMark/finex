package hu.finex.main.service;

import org.springframework.stereotype.Service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.BankSettingsResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankSettingsService {

    private final FinexProperties finexProperties;

    // Az üzleti szabályok a frontendnek, így a felület sem mutat elavult limitet
    public BankSettingsResponse getSettings() {
        return BankSettingsResponse.builder()
                .transferDailyLimit(finexProperties.getTransferDailyLimit())
                .maxAccountsPerUser(finexProperties.getMaxAccountsPerUser())
                .maxSavingsPerUser(finexProperties.getMaxSavingsPerUser())
                .maxStandingOrdersPerUser(finexProperties.getMaxStandingOrdersPerUser())
                .maxBeneficiariesPerUser(finexProperties.getMaxBeneficiariesPerUser())
                .savingsInterestRate(finexProperties.getSavingsInterestRate())
                .cardDefaultDailyLimit(finexProperties.getCardDefaultDailyLimit())
                .cardDefaultDailyLimitForeign(finexProperties.getCardDefaultDailyLimitForeign())
                .cardMaxDailyLimit(finexProperties.getCardMaxDailyLimit())
                .exchangeRates(finexProperties.getExchangeRates())
                .build();
    }
}
