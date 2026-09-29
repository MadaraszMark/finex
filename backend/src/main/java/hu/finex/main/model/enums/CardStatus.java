package hu.finex.main.model.enums;

public enum CardStatus {
    ACTIVE,
    BLOCKED,    // Ideiglenesen zárolva (a felhasználó feloldhatja)
    CANCELLED   // Végleg megszűnt (pl. a számla lezárásakor)
}
