package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Válasz egy ügyfélszolgálati ticketre")
public class CreateTicketMessageRequest {

    @NotBlank
    @Size(max = 5000)
    @Schema(description = "Az üzenet szövege", example = "Köszönöm, holnap újra ránézek!", required = true)
    private String message;
}
