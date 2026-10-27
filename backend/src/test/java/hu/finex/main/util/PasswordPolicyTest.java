package hu.finex.main.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    @Test
    void fitsBcrypt_shouldAllowExactly72Bytes() {
        assertTrue(PasswordPolicy.fitsBcrypt("a".repeat(72)));
        assertFalse(PasswordPolicy.fitsBcrypt("a".repeat(73)));
    }

    // Az ékezetes betű UTF-8-ban 2 bájt: 36 db "ő" még belefér, 37 már nem
    @Test
    void fitsBcrypt_shouldCountAccentedLettersAsTwoBytes() {
        assertTrue(PasswordPolicy.fitsBcrypt("ő".repeat(36)));
        assertFalse(PasswordPolicy.fitsBcrypt("ő".repeat(37)));
    }

    @Test
    void fitsBcrypt_shouldRejectNull() {
        assertFalse(PasswordPolicy.fitsBcrypt(null));
    }
}
