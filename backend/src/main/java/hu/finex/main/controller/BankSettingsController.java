package hu.finex.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.BankSettingsResponse;
import hu.finex.main.service.BankSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
@Tag(name = "Settings API", description = "A bank üzleti szabályainak paraméterei (napi utalási limit, nyitható számlák száma, …)")
public class BankSettingsController {

    private final BankSettingsService bankSettingsService;

    @GetMapping
    @Operation(summary = "Az üzleti szabályok paraméterei a frontend számára",responses = {
                    @ApiResponse(responseCode = "200", description = "Siker",content = @Content(schema = @Schema(implementation = BankSettingsResponse.class)))
            }
    )
    public ResponseEntity<BankSettingsResponse> getSettings() {
        return ResponseEntity.ok(bankSettingsService.getSettings());
    }
}
