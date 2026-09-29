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

import hu.finex.main.dto.CreateStandingOrderRequest;
import hu.finex.main.dto.StandingOrderResponse;
import hu.finex.main.dto.UpdateStandingOrderRequest;
import hu.finex.main.service.StandingOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/standing-orders")
@RequiredArgsConstructor
@Tag(name = "Standing Order API", description = "Rendszeres átutalási megbízások (az ütemező teljesíti őket)")
public class StandingOrderController {

    private final StandingOrderService standingOrderService;

    @GetMapping
    @Operation(summary = "Saját rendszeres átutalások",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés")
            }
    )
    public ResponseEntity<List<StandingOrderResponse>> listMine() {
        return ResponseEntity.ok(standingOrderService.listMine());
    }

    @PostMapping
    @Operation(summary = "Új rendszeres átutalás",responses = {
                    @ApiResponse(responseCode = "201", description = "Létrehozva",content = @Content(schema = @Schema(implementation = StandingOrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet vagy IBAN"),
                    @ApiResponse(responseCode = "404", description = "Forrás számla nem található")
            }
    )
    public ResponseEntity<StandingOrderResponse> create(@Valid @RequestBody CreateStandingOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(standingOrderService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Rendszeres átutalás módosítása (összeg, közlemény, gyakoriság, következő esedékesség)",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres módosítás",content = @Content(schema = @Schema(implementation = StandingOrderResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet vagy megszüntetett megbízás"),
                    @ApiResponse(responseCode = "404", description = "Megbízás nem található")
            }
    )
    public ResponseEntity<StandingOrderResponse> update(@PathVariable("id") Long id,@Valid @RequestBody UpdateStandingOrderRequest request) {
        return ResponseEntity.ok(standingOrderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Rendszeres átutalás megszüntetése",responses = {
                    @ApiResponse(responseCode = "204", description = "Megszüntetve"),
                    @ApiResponse(responseCode = "404", description = "Megbízás nem található")
            }
    )
    public ResponseEntity<Void> cancel(@PathVariable("id") Long id) {
        standingOrderService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}
