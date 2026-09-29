package hu.finex.main.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import hu.finex.main.model.enums.StandingOrderFrequency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Új rendszeres átutalási megbízás")
public class CreateStandingOrderRequest {

    @NotNull
    @Schema(description = "A forrás folyószámla azonosítója", example = "3", required = true)
    private Long accountId;

    @NotBlank
    @Size(max = 42)
    @Schema(description = "A címzett IBAN számlaszáma", example = "HU66 1091 8001 0000 0489 0024 0017", required = true)
    private String toAccountNumber;

    @NotBlank
    @Size(max = 150)
    @Schema(description = "A kedvezményezett neve", example = "Tóth Gábor", required = true)
    private String partnerName;

    @NotNull
    @DecimalMin(value = "0.01", message = "Az összeg legalább 0.01 kell legyen.")
    @Schema(description = "Az utalandó összeg", example = "220000.00", required = true)
    private BigDecimal amount;

    @Size(max = 255)
    @Schema(description = "Közlemény", example = "Albérlet")
    private String message;

    @NotNull
    @Schema(description = "Gyakoriság (WEEKLY, MONTHLY)", example = "MONTHLY", required = true)
    private StandingOrderFrequency frequency;

    @NotNull
    @FutureOrPresent(message = "Az első teljesítés nem lehet a múltban.")
    @Schema(description = "Az első teljesítés napja", example = "2025-03-06", required = true)
    private LocalDate firstExecutionDate;
}
