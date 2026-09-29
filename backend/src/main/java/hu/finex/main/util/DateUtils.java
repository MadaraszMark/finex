package hu.finex.main.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

// A bank budapesti idő szerint számol: "ma", "ez a hónap" és a napi limitek is helyi idő szerint értendők

public final class DateUtils {

    public static final ZoneId BANK_ZONE = ZoneId.of("Europe/Budapest");

    // Nyitott időszak határai: ha egy szűrőben nincs kezdő vagy záró dátum, ezekkel kérdezünk le
    // (a PostgreSQL egy null értékű időbélyeg-paraméter típusát nem tudja meghatározni)
    public static final Instant BEGINNING_OF_TIME = Instant.parse("1970-01-01T00:00:00Z");
    public static final Instant END_OF_TIME = Instant.parse("9999-12-31T00:00:00Z");

    private DateUtils() {
    }

    public static LocalDate today() {
        return LocalDate.now(BANK_ZONE);
    }

    // Egy nap kezdete (00:00 budapesti idő szerint) időpontként
    public static Instant startOfDay(LocalDate date) {
        return date.atStartOfDay(BANK_ZONE).toInstant();
    }

    public static Instant startOfToday() {
        return startOfDay(today());
    }

    public static Instant startOfCurrentMonth() {
        return startOfDay(today().withDayOfMonth(1));
    }
}
