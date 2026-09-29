package hu.finex.main.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.CardPaymentRequest;
import hu.finex.main.dto.CardResponse;
import hu.finex.main.dto.TransactionResponse;
import hu.finex.main.dto.UpdateCardLimitsRequest;
import hu.finex.main.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Tag(name = "Card API", description = "Bankkártyák: letiltás, limitek, kártyás fizetés")
public class CardController {

    private final CardService cardService;

    @GetMapping
    @Operation(summary = "Saját bankkártyák (maszkolt kártyaszámmal)",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés")
            }
    )
    public ResponseEntity<List<CardResponse>> listMine() {
        return ResponseEntity.ok(cardService.listMine());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Saját bankkártya lekérdezése ID alapján",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Kártya nem található")
            }
    )
    public ResponseEntity<CardResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(cardService.getById(id));
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Kártya ideiglenes letiltása (pl. elvesztés esetén)",responses = {
                    @ApiResponse(responseCode = "200", description = "Kártya letiltva",content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Megszűnt kártya"),
                    @ApiResponse(responseCode = "404", description = "Kártya nem található")
            }
    )
    public ResponseEntity<CardResponse> block(@PathVariable("id") Long id) {
        return ResponseEntity.ok(cardService.block(id));
    }

    @PatchMapping("/{id}/unblock")
    @Operation(summary = "Kártya feloldása",responses = {
                    @ApiResponse(responseCode = "200", description = "Kártya feloldva",content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Megszűnt kártya vagy nem aktív számla"),
                    @ApiResponse(responseCode = "404", description = "Kártya nem található")
            }
    )
    public ResponseEntity<CardResponse> unblock(@PathVariable("id") Long id) {
        return ResponseEntity.ok(cardService.unblock(id));
    }

    @PutMapping("/{id}/limits")
    @Operation(summary = "Napi limit és online fizetés beállítása",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres módosítás",content = @Content(schema = @Schema(implementation = CardResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet"),
                    @ApiResponse(responseCode = "404", description = "Kártya nem található")
            }
    )
    public ResponseEntity<CardResponse> updateLimits(@PathVariable("id") Long id,@Valid @RequestBody UpdateCardLimitsRequest request) {
        return ResponseEntity.ok(cardService.updateLimits(id, request));
    }

    @PostMapping("/{id}/payments")
    @Operation(summary = "Kártyás fizetés szimulálása (demó)", description = "Ellenőrzi a kártya és a számla állapotát, a lejáratot, az online fizetés engedélyét, a napi limitet és a fedezetet.",responses = {
                    @ApiResponse(responseCode = "201", description = "Sikeres fizetés",content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Elutasított fizetés"),
                    @ApiResponse(responseCode = "404", description = "Kártya nem található")
            }
    )
    public ResponseEntity<TransactionResponse> pay(@PathVariable("id") Long id,@Valid @RequestBody CardPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.pay(id, request));
    }
}
