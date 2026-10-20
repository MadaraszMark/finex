package hu.finex.main.service;

import hu.finex.main.config.FinexProperties;
import hu.finex.main.dto.BankSettingsResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BankSettingsServiceTest {

    @Spy private FinexProperties finexProperties = new FinexProperties();

    @InjectMocks private BankSettingsService service;

    @Test
    void getSettings_shouldReturnTheConfiguredBusinessRules() {
        finexProperties.setTransferDailyLimit(new BigDecimal("3000000"));
        finexProperties.setMaxAccountsPerUser(3);

        BankSettingsResponse resp = service.getSettings();

        assertEquals(new BigDecimal("3000000"), resp.getTransferDailyLimit());
        assertEquals(3, resp.getMaxAccountsPerUser());
        assertEquals(new BigDecimal("3.50"), resp.getSavingsInterestRate());
        assertEquals(new BigDecimal("200000"), resp.getCardDefaultDailyLimit());
        assertEquals(new BigDecimal("1000"), resp.getCardDefaultDailyLimitForeign());
    }
}
