package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Új ügyfélszolgálati ticket létrehozásához szükséges adatok")
public class CreateSupportTicketRequest {

    @NotBlank
    @Size(max = 200)
    @Schema(description = "A ticket címe",example = "Probléma történt a legutóbbi utalásomnál..",maxLength = 200, required = true)
    private String title;

    @NotBlank
    @Schema(description = "A felhasználó által írt részletes üzenet",example = "Az utalásom nem érkezett meg...",required = true)
    private String message;
}

