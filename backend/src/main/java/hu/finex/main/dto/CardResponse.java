package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import hu.finex.main.model.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Bankkártya adatai (a kártyaszám maszkolva)")
public class CardResponse {

    @Schema(description = "A kártya azonosítója", example = "7")
    private Long id;

    @Schema(description = "A kártyához tartozó számla ID-ja", example = "3")
    private Long accountId;

    @Schema(description = "A kártyához tartozó számla száma", example = "HU28104000950000521700000003")
    private String accountNumber;

    @Schema(description = "Maszkolt kártyaszám", example = "**** **** **** 5521")
    private String maskedNumber;

    @Schema(description = "A kártyára nyomtatott név", example = "KOVÁCS ANNA")
    private String holderName;

    @Schema(description = "Lejárat (a hónap utolsó napja)", example = "2029-09-30")
    private LocalDate expiryDate;

    @Schema(description = "Státusz", example = "ACTIVE")
    private CardStatus status;

    @Schema(description = "Napi költési limit", example = "200000.00")
    private BigDecimal dailyLimit;

    @Schema(description = "A mai napon a kártyával elköltött összeg", example = "15990.00")
    private BigDecimal spentToday;

    @Schema(description = "Engedélyezett-e az online fizetés", example = "true")
    private boolean onlinePaymentEnabled;

    @Schema(description = "Kibocsátás időpontja", example = "2025-02-12T11:30:22Z")
    private Instant createdAt;
}
