package hu.finex.main.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

// Összegek emberi olvasásra szánt formája (értesítések, hibaüzenetek): "15 000 Ft", "245,50 EUR"

public final class MoneyUtils {

    private static final Locale HUNGARIAN = Locale.forLanguageTag("hu-HU");

    private MoneyUtils() {
    }

    public static String format(BigDecimal amount, String currency) {
        NumberFormat format = NumberFormat.getNumberInstance(HUNGARIAN);
        format.setMinimumFractionDigits(amount.stripTrailingZeros().scale() > 0 ? 2 : 0);
        format.setMaximumFractionDigits(2);

        return format.format(amount) + " " + ("HUF".equals(currency) ? "Ft" : currency);
    }
}
