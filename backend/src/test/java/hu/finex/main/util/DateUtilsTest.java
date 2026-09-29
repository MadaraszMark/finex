package hu.finex.main.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void startOfDay_shouldUseBudapestTime() {
        // Budapesten télen UTC+1, nyáron UTC+2
        assertEquals(Instant.parse("2025-01-14T23:00:00Z"), DateUtils.startOfDay(LocalDate.of(2025, 1, 15)));
        assertEquals(Instant.parse("2025-07-14T22:00:00Z"), DateUtils.startOfDay(LocalDate.of(2025, 7, 15)));
    }

    @Test
    void startOfCurrentMonth_shouldBeFirstDayMidnight() {
        Instant monthStart = DateUtils.startOfCurrentMonth();

        assertEquals(1, monthStart.atZone(DateUtils.BANK_ZONE).getDayOfMonth());
        assertEquals(0, monthStart.atZone(DateUtils.BANK_ZONE).getHour());
        assertFalse(monthStart.isAfter(DateUtils.startOfToday()));
    }

    @Test
    void openPeriodBounds_shouldCoverEveryRealTimestamp() {
        assertTrue(DateUtils.BEGINNING_OF_TIME.isBefore(Instant.parse("2000-01-01T00:00:00Z")));
        assertTrue(DateUtils.END_OF_TIME.isAfter(Instant.now()));
    }
}
