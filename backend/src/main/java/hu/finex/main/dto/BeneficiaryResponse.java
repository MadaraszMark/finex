package hu.finex.main.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Mentett kedvezményezett")
public class BeneficiaryResponse {

    @Schema(description = "Azonosító", example = "4")
    private Long id;

    @Schema(description = "A kedvezményezett neve", example = "Tóth Gábor")
    private String name;

    @Schema(description = "IBAN számlaszám", example = "HU66109180010000048900240017")
    private String accountNumber;

    @Schema(description = "Saját megjegyzés", example = "Főbérlő")
    private String note;

    @Schema(description = "FineX-es számla-e (ilyenkor az utalás azonnal jóváíródik)", example = "false")
    private boolean internal;

    @Schema(description = "Mentés időpontja", example = "2025-02-12T11:30:22Z")
    private Instant createdAt;
}
