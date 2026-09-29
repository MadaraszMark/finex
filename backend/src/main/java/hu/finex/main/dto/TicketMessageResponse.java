package hu.finex.main.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Egy üzenet a ticket beszélgetésében")
public class TicketMessageResponse {

    @Schema(description = "Azonosító", example = "8")
    private Long id;

    @Schema(description = "A szerző neve", example = "Admin FineX")
    private String authorName;

    @Schema(description = "Az ügyfélszolgálattól (admin) érkezett-e", example = "true")
    private boolean staff;

    @Schema(description = "Az üzenet szövege", example = "A kártyás tételek 1-2 munkanapon belül könyvelődnek.")
    private String message;

    @Schema(description = "Időpont", example = "2025-02-13T09:12:00Z")
    private Instant createdAt;
}
