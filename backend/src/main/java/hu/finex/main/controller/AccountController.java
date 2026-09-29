package hu.finex.main.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.AccountResponse;
import hu.finex.main.dto.CreateAccountRequest;
import hu.finex.main.dto.DepositRequest;
import hu.finex.main.dto.StatementResponse;
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
@Tag(name = "Account API", description = "A bejelentkezett felhasználó bankszámlái")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Saját bankszámlák listája",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés")
        }
    )
    public ResponseEntity<List<AccountResponse>> listMine() {
        return ResponseEntity.ok(accountService.listMine());
    }

    @PostMapping
    @Operation(summary = "Új bankszámla nyitása (bankkártyával együtt)",responses = {
            @ApiResponse(responseCode = "201", description = "Sikeresen létrehozva",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet vagy elérte a számlák maximális számát")
        }
    )
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.open(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Saját bankszámla lekérdezése ID alapján",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<AccountResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(accountService.getById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Elsődleges folyószámla (a legrégebbi aktív folyószámla)", responses = {
        @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))),
        @ApiResponse(responseCode = "404", description = "Számla nem található")
    })
    public ResponseEntity<AccountResponse> getMyAccount() {
        return ResponseEntity.ok(accountService.getMyAccount());
    }

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Egyenleg befizetése a bankszámlára", description = "Demó funkció (pl. ATM-es készpénzbefizetés): a bejelentkezett felhasználó pénzt fizet be a saját számlájára.",responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Sikeres befizetés",
                content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet vagy nem aktív számla"),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<AccountResponse> deposit(@PathVariable("id") Long accountId,@Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(accountService.deposit(accountId, request));
    }

    @GetMapping("/{id}/statement")
    @Operation(summary = "Számlakivonat egy időszakra", description = "Nyitó- és záróegyenleg, összesítők és a tételek futó egyenleggel. A számítást az adatbázis account_statement függvénye végzi.",responses = {
            @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",
                content = @Content(schema = @Schema(implementation = StatementResponse.class))),
            @ApiResponse(responseCode = "400", description = "Hibás időszak"),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<StatementResponse> getStatement(@PathVariable("id") Long id,
                                                          @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                          @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(accountService.getStatement(id, from, to));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Bankszámla lezárása (CLOSED státusz)", description = "Csak nulla egyenleggel; a kártyák megszűnnek, a rendszeres átutalások leállnak. Az utolsó nyitott számla nem zárható le.",responses = {
            @ApiResponse(responseCode = "204", description = "Sikeres lezárás"),
            @ApiResponse(responseCode = "400", description = "A számla nem zárható le"),
            @ApiResponse(responseCode = "404", description = "Számla nem található")
        }
    )
    public ResponseEntity<Void> close(@PathVariable("id") Long id) {
        accountService.close(id);
        return ResponseEntity.noContent().build();
    }
}
