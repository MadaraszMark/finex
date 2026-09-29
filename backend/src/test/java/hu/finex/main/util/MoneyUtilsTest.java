package hu.finex.main.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyUtilsTest {

    // A magyar számformátum ezres elválasztója nem törhető szóköz, az összehasonlításhoz sima szóközre cseréljük
    private String format(String amount, String currency) {
        return MoneyUtils.format(new BigDecimal(amount), currency).replace(' ', ' ').replace(' ', ' ');
    }

    @Test
    void format_shouldUseForintSign_andHideZeroDecimals() {
        assertEquals("15 000 Ft", format("15000.00", "HUF"));
        assertEquals("220 000 Ft", format("220000", "HUF"));
    }

    @Test
    void format_shouldShowTwoDecimals_whenAmountHasFraction() {
        assertEquals("245,50 EUR", format("245.5", "EUR"));
        assertEquals("39,99 EUR", format("39.99", "EUR"));
    }
}
