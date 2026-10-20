package hu.finex.main.service;

import org.springframework.stereotype.Service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.BankSettingsResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankSettingsService {

    private final FinexProperties finexProperties;

    // Az üzleti szabályok paraméterei (application.properties: finex.*): a frontend innen tudja pl. a napi utalási
    // limitet és a nyitható számlák számát, így ha a beállítás változik, a felület sem mutat elavult értéket
    public BankSettingsResponse getSettings() {
        return BankSettingsResponse.builder()
                .transferDailyLimit(finexProperties.getTransferDailyLimit())
                .maxAccountsPerUser(finexProperties.getMaxAccountsPerUser())
                .savingsInterestRate(finexProperties.getSavingsInterestRate())
                .cardDefaultDailyLimit(finexProperties.getCardDefaultDailyLimit())
                .cardDefaultDailyLimitForeign(finexProperties.getCardDefaultDailyLimitForeign())
                .build();
    }
}
