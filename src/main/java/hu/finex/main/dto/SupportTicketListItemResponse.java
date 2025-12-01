package hu.finex.main.dto;

import java.time.OffsetDateTime;

import hu.finex.main.model.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Egyszerűsített support ticket adat lista megjelenítéshez")
public class SupportTicketListItemResponse {

    @Schema(description = "A ticket azonosítója", example = "1501")
    private Long id;

    @Schema(description = "A ticket címe", example = "Az utalás túl sokáig tart..")
    private String title;

    @Schema(description = "A ticket státusza", example = "IN_PROGRESS")
    private TicketStatus status;

    @Schema(description = "Létrehozás ideje", example = "2025-02-12T12:10:05Z")
    private OffsetDateTime createdAt;
}

