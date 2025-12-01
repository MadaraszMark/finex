package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "A számlához tartozó bankkártya módosításához szükséges adatok")
public class UpdateCardNumberRequest {

    @Size(max = 20)
    @Schema(description = "Maszkolt bankkártyaszám",example = "**** **** **** 5521")
    private String cardNumber;
}

