package hu.finex.main.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kategória hozzárendelése egy tranzakcióhoz")
public class AssignCategoryToTransactionRequest {

    @NotNull
    @Schema(description = "A tranzakció azonosítója", example = "5012",required = true)
    private Long transactionId;

    @NotNull
    @Schema(description = "A kategória azonosítója",example = "3",required = true)
    private Long categoryId;
}
