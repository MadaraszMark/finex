package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Olvasatlan értesítések száma")
public class UnreadCountResponse {

    @Schema(description = "Olvasatlan értesítések száma", example = "3")
    private long count;
}
