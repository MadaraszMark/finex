package hu.finex.main.dto;

import java.time.Instant;

import hu.finex.main.model.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Részletes ügyfélszolgálati ticket adatok")
public class SupportTicketResponse {

    @Schema(description = "A ticket egyedi azonosítója",example = "1501")
    private Long id;

    @Schema(description = "A ticketet létrehozó felhasználó ID-ja",example = "42")
    private Long userId;

    @Schema(description = "A ticket címe",example = "Issue with account balance update")
    private String title;

    @Schema(description = "A ticket üzenete")
    private String message;

    @Schema(description = "A ticket státusza",example = "OPEN")
    private TicketStatus status;

    @Schema(description = "Létrehozás időpontja", example = "2025-02-12T12:10:05Z")
    private Instant createdAt;

    @Schema(description = "Utolsó módosítás időpontja",example = "2025-02-12T12:30:10Z")
    private Instant updatedAt;
}

