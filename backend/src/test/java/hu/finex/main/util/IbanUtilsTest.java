package hu.finex.main.util;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IbanUtilsTest {

    @Test
    void normalize_shouldRemoveSpacesAndUppercase() {
        assertEquals("HU15117730161111101800000001", IbanUtils.normalize(" hu15 1177 3016 1111 1018 0000 0001 "));
        assertNull(IbanUtils.normalize(null));
    }

    @Test
    void isValid_shouldAcceptCorrectHungarianIban() {
        assertTrue(IbanUtils.isValid("HU15117730161111101800000001"));
        assertTrue(IbanUtils.isValid("HU28 1040 0095 0000 5217 0000 0003"));
    }

    @Test
    void isValid_shouldRejectWrongChecksum() {
        // Egyetlen számjegy eltérés: a mod-97 ellenőrzés kiszűri
        assertFalse(IbanUtils.isValid("HU15117730161111101800000002"));
        assertFalse(IbanUtils.isValid("HU16117730161111101800000001"));
    }

    @Test
    void isValid_shouldRejectWrongFormat() {
        assertFalse(IbanUtils.isValid(null));
        assertFalse(IbanUtils.isValid(""));
        assertFalse(IbanUtils.isValid("1234"));
        assertFalse(IbanUtils.isValid("HU1511773016111110180000000"));
        assertFalse(IbanUtils.isValid("HU15-1177-3016-1111-1018-0000-0001"));
    }

    @Test
    void isValid_shouldAcceptForeignIban() {
        // Német IBAN (22 karakter), a szabvány példája
        assertTrue(IbanUtils.isValid("DE89370400440532013000"));
    }

    @RepeatedTest(20)
    void generateHungarian_shouldAlwaysProduceValidIban() {
        String iban = IbanUtils.generateHungarian();

        assertEquals(28, iban.length());
        assertTrue(iban.startsWith("HU"));
        assertTrue(IbanUtils.isValid(iban));
    }
}
