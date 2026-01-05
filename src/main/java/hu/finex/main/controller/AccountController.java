package hu.finex.main.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.AccountListItemResponse;
import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.CreateAccountRequest;
import hu.finex.main.dto.DepositRequest;
import hu.finex.main.dto.UpdateAccountStatusRequest;
import hu.finex.main.dto.UpdateCardNumberRequest;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.model.User;
import hu.finex.main.repository.UserRepository;
import hu.finex.main.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Account API", description = "Bankszámlák kezelése")
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Új bankszámla létrehozása",responses = {
            @ApiResponse(responseCode = "201", description = "Sikeresen létrehozva",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet"),
            @ApiResponse(responseCode = "404", description = "Felhasználó nem található")
        }
    )
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Bankszámla lekérdezése ID alapján",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<AccountResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(accountService.getById(id));
    }
    
    @GetMapping("/me")
    @Operation(summary = "Bejelentkezett felhasználó bankszámlájának lekérdezése", responses = {
        @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "404", description = "Számla nem található")
    })
    public ResponseEntity<AccountResponse> getMyAccount(Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        return ResponseEntity.ok(accountService.getMyAccount(user.getId()));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Felhasználó összes bankszámlájának lekérdezése",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",
                content = @Content(schema = @Schema(implementation = AccountListItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Felhasználó nem található")
        }
    )
    public ResponseEntity<List<AccountListItemResponse>> listByUser(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(accountService.listByUser(userId));
    }

    @PutMapping("/{id}/card")
    @Operation(summary = "Bankszámla kártyaszám módosítása",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres módosítás",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet"),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<AccountResponse> updateCard(@PathVariable("id") Long id,@Valid @RequestBody UpdateCardNumberRequest request) {

        return ResponseEntity.ok(accountService.updateCardNumber(id, request));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Bankszámla státusz módosítása",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres módosítás",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet"),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<AccountResponse> updateStatus(@PathVariable("id") Long id,@Valid @RequestBody UpdateAccountStatusRequest request) {

        return ResponseEntity.ok(accountService.updateStatus(id, request));
    }
    
    @PostMapping("/{id}/deposit")
    @Operation(summary = "Egyenleg befizetése a bankszámlára", description = "A bejelentkezett felhasználó pénzt fizet be az adott számlára.",responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Sikeres befizetés",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet"),
            @ApiResponse(responseCode = "403", description = "A számla nem a bejelentkezett felhasználóé"),
            @ApiResponse(responseCode = "404", description = "Számla vagy felhasználó nem található")
        }
    )
    public ResponseEntity<AccountResponse> deposit(@PathVariable("id") Long accountId,@Valid @RequestBody DepositRequest request,Principal principal) {
        String email = principal.getName();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Felhasználó nem található."));

        AccountResponse response = accountService.deposit(accountId,request.getAmount(),request.getMessage(), user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Bankszámla lezárása (CLOSED státusz)",responses = {
            @ApiResponse(responseCode = "204", description = "Sikeres törlés"),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
