package hu.finex.main.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import hu.finex.main.exception.BusinessException;

// A bank budapesti idő szerint számol: "ma", "ez a hónap" és a napi limitek is helyi időben értendők

public final class DateUtils {

    public static final ZoneId BANK_ZONE = ZoneId.of("Europe/Budapest");

    // Nyitott időszak határai: a PostgreSQL egy null időbélyeg-paraméter típusát nem tudja meghatározni
    public static final Instant BEGINNING_OF_TIME = Instant.parse("1970-01-01T00:00:00Z");
    public static final Instant END_OF_TIME = Instant.parse("9999-12-31T00:00:00Z");

    private DateUtils() {
    }

    public static LocalDate today() {
        return LocalDate.now(BANK_ZONE);
    }

    public static Instant startOfDay(LocalDate date) {
        return date.atStartOfDay(BANK_ZONE).toInstant();
    }

    public static Instant startOfToday() {
        return startOfDay(today());
    }

    public static Instant startOfCurrentMonth() {
        return startOfDay(today().withDayOfMonth(1));
    }

    public static Instant startOfPreviousMonth() {
        return startOfDay(today().withDayOfMonth(1).minusMonths(1));
    }

    // Kivonat és egyenlegtörténet max. ennyi napra kérhető egyszerre (ne lehessen évtizedeket lekérni)
    public static final int MAX_REPORT_DAYS = 366;

    public static void requireReportRange(LocalDate from, LocalDate to) {
        if (ChronoUnit.DAYS.between(from, to) > MAX_REPORT_DAYS) {
            throw new BusinessException("Legfeljebb egy évnyi időszak kérhető le egyszerre.");
        }
    }
}
