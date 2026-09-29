package hu.finex.main.model.enums;

public enum NotificationType {
    TRANSACTION,    // Pénzmozgás (pl. beérkező utalás)
    SECURITY,       // Biztonsági esemény (pl. sikertelen belépések, jelszócsere)
    SAVINGS,        // Megtakarítás (pl. kamatjóváírás)
    SUPPORT,        // Ügyfélszolgálati válasz
    SYSTEM          // Rendszerüzenet
}
