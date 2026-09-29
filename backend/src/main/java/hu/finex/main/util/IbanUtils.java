package hu.finex.main.util;

import java.util.Locale;

// IBAN-számlaszámok generálása és ellenőrzése (ISO 13616, mod-97 ellenőrzőszám)

public final class IbanUtils {

    private IbanUtils() {
    }

    // Szóközök nélküli, nagybetűs alak: "hu15 1177 3016 ..." -> "HU1511773016..."
    public static String normalize(String iban) {
        if (iban == null) {
            return null;
        }
        return iban.replaceAll("\\s", "").toUpperCase(Locale.ROOT);
    }

    // Formátum + ellenőrzőszám: az ország és az ellenőrzőszám a végére kerül, és a szám mod 97 értéke 1 kell legyen
    public static boolean isValid(String iban) {
        String normalized = normalize(iban);
        if (normalized == null || !normalized.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}")) {
            return false;
        }
        if (normalized.startsWith("HU") && normalized.length() != 28) {
            return false;
        }

        String rearranged = normalized.substring(4) + normalized.substring(0, 4);
        return mod97(convertLettersToDigits(rearranged)) == 1;
    }

    // 24 jegyű random magyar bankszámlaszám, érvényes ellenőrzőszámmal
    public static String generateHungarian() {
        StringBuilder base = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            base.append((int) (Math.random() * 10));
        }

        String countryCode = "HU";
        String checksumBase = base.toString() + convertLettersToDigits(countryCode + "00");

        int mod = mod97(checksumBase);
        int checksum = 98 - mod;

        String formattedChecksum = String.format("%02d", checksum);

        return countryCode + formattedChecksum + base;
    }

    // Betűk átalakítása számokká (A=10, B=11...)
    private static String convertLettersToDigits(String input) {
        StringBuilder result = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isLetter(ch)) {
                result.append((ch - 'A') + 10);
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    // Nagy szám mod 97
    private static int mod97(String input) {
        String remainder = "0";

        for (int i = 0; i < input.length(); i += 7) {
            int end = Math.min(i + 7, input.length());
            String chunk = remainder + input.substring(i, end);
            remainder = String.valueOf(Long.parseLong(chunk) % 97);
        }

        return Integer.parseInt(remainder);
    }
}
