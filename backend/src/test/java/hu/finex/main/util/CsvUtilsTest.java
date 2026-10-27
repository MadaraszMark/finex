package hu.finex.main.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvUtilsTest {

    // CSV injection ellen: a képletnek látszó szöveg elé aposztróf kerül, így az Excel nem futtatja
    @Test
    void text_shouldNeutralizeFormulaLikeValues() {
        assertEquals("'=HYPERLINK(\"http://x\")", CsvUtils.text("=HYPERLINK(\"http://x\")"));
        assertEquals("'+36301234567", CsvUtils.text("+36301234567"));
        assertEquals("'-5", CsvUtils.text("-5"));
        assertEquals("'@SUM(A1)", CsvUtils.text("@SUM(A1)"));
        assertEquals("Albérlet", CsvUtils.text("Albérlet"));
        assertEquals("", CsvUtils.text(null));
    }

    @Test
    void row_shouldQuoteCellsWithSeparatorQuoteOrNewline() {
        String row = CsvUtils.row(List.of("a;b", "say \"hi\"", "sima"));

        assertEquals("\"a;b\";\"say \"\"hi\"\"\";sima\r\n", row);
    }

    @Test
    void number_shouldUseDecimalCommaWithoutGrouping() {
        assertEquals("-12345,50", CsvUtils.number(new BigDecimal("-12345.5")));
        assertEquals("1000000,00", CsvUtils.number(new BigDecimal("1000000")));
        assertEquals("", CsvUtils.number(null));
    }
}
