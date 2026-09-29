package hu.finex.main.controller;

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

import hu.finex.main.dto.BeneficiaryResponse;
import hu.finex.main.dto.CreateBeneficiaryRequest;
import hu.finex.main.dto.UpdateBeneficiaryRequest;
import hu.finex.main.service.BeneficiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Beneficiary API", description = "Mentett kedvezményezettek (gyors utaláshoz)")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @GetMapping
    @Operation(summary = "Saját kedvezményezettek név szerint",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés")
            }
    )
    public ResponseEntity<List<BeneficiaryResponse>> listMine() {
        return ResponseEntity.ok(beneficiaryService.listMine());
    }

    @PostMapping
    @Operation(summary = "Új kedvezményezett mentése",responses = {
                    @ApiResponse(responseCode = "201", description = "Mentve",content = @Content(schema = @Schema(implementation = BeneficiaryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen IBAN vagy már mentett számlaszám")
            }
    )
    public ResponseEntity<BeneficiaryResponse> create(@Valid @RequestBody CreateBeneficiaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficiaryService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Kedvezményezett módosítása",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres módosítás",content = @Content(schema = @Schema(implementation = BeneficiaryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen IBAN vagy már mentett számlaszám"),
                    @ApiResponse(responseCode = "404", description = "Kedvezményezett nem található")
            }
    )
    public ResponseEntity<BeneficiaryResponse> update(@PathVariable("id") Long id,@Valid @RequestBody UpdateBeneficiaryRequest request) {
        return ResponseEntity.ok(beneficiaryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Kedvezményezett törlése",responses = {
                    @ApiResponse(responseCode = "204", description = "Sikeres törlés"),
                    @ApiResponse(responseCode = "404", description = "Kedvezményezett nem található")
            }
    )
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        beneficiaryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
