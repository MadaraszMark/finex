package hu.finex.main.config;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

// Az üzleti szabályok paraméterei (application.properties: finex.*), hogy ne legyenek beégetve

@Getter
@Setter
@ConfigurationProperties(prefix = "finex")
public class FinexProperties {

    // Napi utalási limit számlánként forintban (devizaszámlán az árfolyamon számolt megfelelője)
    private BigDecimal transferDailyLimit = new BigDecimal("5000000");

    // Új kártya alapértelmezett napi költési limitje (forintszámlához, illetve devizaszámlához)
    private BigDecimal cardDefaultDailyLimit = new BigDecimal("200000");
    private BigDecimal cardDefaultDailyLimitForeign = new BigDecimal("1000");

    // A kártya napi limitjének felső határa forintban (devizakártyánál az árfolyamon átszámolva)
    private BigDecimal cardMaxDailyLimit = new BigDecimal("5000000");

    // Új megtakarítások éves kamatlába (%)
    private BigDecimal savingsInterestRate = new BigDecimal("3.50");

    // Egy felhasználó legfeljebb ennyi nem lezárt folyószámlát nyithat
    private int maxAccountsPerUser = 5;

    // Felső korlátok felhasználónként (tömeges létrehozással ne lehessen lassítani az ütemezőt)
    private int maxSavingsPerUser = 10;
    private int maxStandingOrdersPerUser = 20;
    private int maxBeneficiariesPerUser = 50;

    // Ennyi sikertelen belépés után zárolódik a fiók arról az IP-címről
    private int loginMaxFailedAttempts = 5;

    // Ennyi sikertelen belépés után zárolódik a fiók bármelyik IP-címről (elosztott támadás ellen)
    private int loginMaxFailedAttemptsPerAccount = 20;

    // Ennyi sikertelen belépés után zárolódik egy IP-cím, akármelyik fiókra próbálkozik
    private int loginMaxFailedAttemptsPerIp = 20;

    // Az átmeneti zárolás hossza percben
    private int loginLockMinutes = 15;

    // Kérésszám-korlát IP-címenként: bejelentkezés percenként, regisztráció óránként
    private int rateLimitLoginPerMinute = 10;
    private int rateLimitRegisterPerHour = 10;

    // Devizaárfolyamok (1 egység = ennyi Ft): devizaváltáshoz és a forintos limitek átszámításához
    private Map<String, BigDecimal> exchangeRates = new LinkedHashMap<>(Map.of("HUF", BigDecimal.ONE));

    // A frontend címe(i), ahonnan a böngésző hívhatja az API-t
    private List<String> corsAllowedOrigins = List.of("http://localhost:5173");
}
