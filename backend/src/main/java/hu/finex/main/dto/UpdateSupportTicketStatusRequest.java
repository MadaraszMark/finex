package hu.finex.main.dto;

import hu.finex.main.model.enums.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Support ticket státusz módosításához szükséges adatok")
public class UpdateSupportTicketStatusRequest {

    @NotNull
    @Schema(description = "Új státusz (OPEN, IN_PROGRESS, RESOLVED)",example = "IN_PROGRESS")
    private TicketStatus status;
}
