package hu.finex.main.dto;

import java.time.Instant;

import hu.finex.main.model.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Értesítés")
public class NotificationResponse {

    @Schema(description = "Azonosító", example = "12")
    private Long id;

    @Schema(description = "Típus", example = "TRANSACTION")
    private NotificationType type;

    @Schema(description = "Cím", example = "Beérkező utalás")
    private String title;

    @Schema(description = "Szöveg", example = "15 000 Ft érkezett Nagy Bence számlájáról.")
    private String message;

    @Schema(description = "Olvasott-e", example = "false")
    private boolean read;

    @Schema(description = "Időpont", example = "2025-02-15T13:25:44Z")
    private Instant createdAt;
}
