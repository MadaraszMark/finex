package hu.finex.main.util;

import java.nio.charset.StandardCharsets;

// Jelszó felső határa bájtban: a BCrypt csak 72 bájtot használ, és az ékezetes betű 2 bájt (UTF-8)

public final class PasswordPolicy {

    public static final int MAX_BYTES = 72;

    public static final String TOO_LONG_MESSAGE = "A jelszó túl hosszú (legfeljebb 72 bájt; az ékezetes betűk 2 bájtnak számítanak).";

    private PasswordPolicy() {
    }

    public static boolean fitsBcrypt(String password) {
        return password != null && password.getBytes(StandardCharsets.UTF_8).length <= MAX_BYTES;
    }
}
