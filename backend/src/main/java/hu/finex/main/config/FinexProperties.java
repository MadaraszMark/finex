package hu.finex.main.config;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

// Az üzleti szabályok paraméterei (application.properties: finex.*), hogy ne a kódban legyenek beégetve

@Getter
@Setter
@ConfigurationProperties(prefix = "finex")
public class FinexProperties {

    // Egy számláról egy nap alatt kiutalható összeg
    private BigDecimal transferDailyLimit = new BigDecimal("5000000");

    // Új kártya alapértelmezett napi költési limitje (forintszámlához, illetve devizaszámlához)
    private BigDecimal cardDefaultDailyLimit = new BigDecimal("200000");
    private BigDecimal cardDefaultDailyLimitForeign = new BigDecimal("1000");

    // Új megtakarítások éves kamatlába (%)
    private BigDecimal savingsInterestRate = new BigDecimal("3.50");

    // Egy felhasználó legfeljebb ennyi nem lezárt folyószámlát nyithat
    private int maxAccountsPerUser = 5;

    // Ennyi sikertelen belépés után a fiók átmenetileg zárolódik
    private int loginMaxFailedAttempts = 5;

    // Az átmeneti zárolás hossza percben
    private int loginLockMinutes = 15;

    // A frontend címe(i), ahonnan a böngésző hívhatja az API-t
    private List<String> corsAllowedOrigins = List.of("http://localhost:5173");
}
