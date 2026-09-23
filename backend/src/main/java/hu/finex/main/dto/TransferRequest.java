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
@Schema(description = "Két bankszámla közötti átutalás kérésének adatai")
public class TransferRequest {

    @NotNull
    @Schema(description = "Forrás számla azonosítója", example = "2", required = true)
    private Long fromAccountId;

    @NotNull
    @Schema(description = "Cél számla azonosítója", example = "3", required = true)
    private Long toAccountId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Az átutalás összege legalább 0.01 kell legyen.")
    @Schema(description = "Átutalás összege", example = "15000.00", required = true)
    private BigDecimal amount;

    @NotBlank
    @Size(max = 3)
    @Schema(description = "Devizanem (mindkét számlának ebben kell vezetve lennie)", example = "HUF", required = true)
    private String currency;

    @Size(max = 255)
    @Schema(description = "Megjegyzés az átutaláshoz", example = "Közös vacsi")
    private String message;

    @Schema(description = "Kategória ID-k, amelyek az átutaláshoz tartoznak", example = "[2, 3]")
    private List<Long> categoryIds;
}

