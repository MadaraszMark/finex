package hu.finex.main.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Egy felhasználó teljes áttekintése az admin felülethez")
public class UserDetailsResponse {

    @Schema(description = "A felhasználó adatai")
    private UserResponse user;

    @Schema(description = "A felhasználó számlái")
    private List<AccountListItemResponse> accounts;

    @Schema(description = "A felhasználó kártyái")
    private List<CardResponse> cards;

    @Schema(description = "A felhasználó nem lezárt megtakarításai")
    private List<SavingsAccountResponse> savingsAccounts;
}
