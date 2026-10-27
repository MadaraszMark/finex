package hu.finex.main.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

// CSV-írás a magyar Excelhez: pontosvessző az elválasztó, tizedesvessző a számokban

public final class CsvUtils {

    public static final char SEPARATOR = ';';

    // UTF-8 BOM: enélkül az Excel nem ismeri fel az ékezetes betűket
    public static final String BOM = "﻿";

    private CsvUtils() {
    }

    public static String row(List<String> cells) {
        return cells.stream().map(CsvUtils::cell).collect(Collectors.joining(String.valueOf(SEPARATOR))) + "\r\n";
    }

    // Felhasználói szöveg cellaként: a képletnek látszó kezdet elé aposztróf kerül (CSV injection ellen)
    public static String text(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        char first = value.charAt(0);
        if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r') {
            return "'" + value;
        }
        return value;
    }

    // Szám két tizedesre, tizedesvesszővel, ezres tagolás nélkül (így az Excel számként olvassa be)
    public static String number(BigDecimal value) {
        return value == null ? "" : value.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    private static String cell(String value) {
        if (value == null) {
            return "";
        }
        if (value.indexOf(SEPARATOR) >= 0 || value.indexOf('"') >= 0 || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
