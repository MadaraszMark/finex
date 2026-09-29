package hu.finex.main.dto;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Átutalás a bejelentkezett felhasználó számlájáról egy IBAN számlaszámra")
public class TransferRequest {

    @NotNull
    @Schema(description = "Forrás számla azonosítója (a saját számlák egyike)", example = "2", required = true)
    private Long fromAccountId;

    @NotBlank
    @Size(max = 42)
    @Schema(description = "A címzett IBAN számlaszáma (szóközökkel is megadható)", example = "HU28 1040 0095 0000 5217 0000 0003", required = true)
    private String toAccountNumber;

    @NotBlank
    @Size(max = 150)
    @Schema(description = "A kedvezményezett neve", example = "Nagy Bence", required = true)
    private String partnerName;

    @NotNull
    @DecimalMin(value = "0.01", message = "Az átutalás összege legalább 0.01 kell legyen.")
    @Schema(description = "Átutalás összege (a forrásszámla devizanemében)", example = "15000.00", required = true)
    private BigDecimal amount;

    @Size(max = 255)
    @Schema(description = "Megjegyzés az átutaláshoz", example = "Közös vacsi")
    private String message;

    @Schema(description = "Kategória ID-k, amelyek az átutaláshoz tartoznak", example = "[2, 3]")
    private List<Long> categoryIds;
}
