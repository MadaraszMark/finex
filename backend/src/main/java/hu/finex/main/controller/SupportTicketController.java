package hu.finex.main.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import hu.finex.main.dto.CreateSupportTicketRequest;
import hu.finex.main.dto.CreateTicketMessageRequest;
import hu.finex.main.dto.SupportTicketListItemResponse;
import hu.finex.main.dto.SupportTicketResponse;
import hu.finex.main.service.SupportTicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/support-tickets")
@RequiredArgsConstructor
@Tag(name = "Support Ticket API", description = "A bejelentkezett felhasználó ügyfélszolgálati ticketjei")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    @Operation(summary = "Új support ticket létrehozása",responses = {@ApiResponse(responseCode = "201",description = "Ticket létrehozva",content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet, vagy már van nyitott ticket")
            }
    )
    public ResponseEntity<SupportTicketResponse> create(@Valid @RequestBody CreateSupportTicketRequest request) {
        SupportTicketResponse response =supportTicketService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    @Operation(summary = "Saját ticketek (lapozva, a legújabb elöl)",responses = {
                    @ApiResponse(responseCode = "200", description = "Siker")
            }
    )
    public ResponseEntity<Page<SupportTicketListItemResponse>> listMine(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(supportTicketService.listMine(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Saját ticket a teljes beszélgetéssel",responses = {
                    @ApiResponse(responseCode = "200", description = "Siker",content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Ticket nem található")
            }
    )
    public ResponseEntity<SupportTicketResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(supportTicketService.getMine(id));
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Válasz a saját ticketre",responses = {
                    @ApiResponse(responseCode = "200", description = "Üzenet elküldve",content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Lezárt ticketre nem lehet válaszolni"),
                    @ApiResponse(responseCode = "404", description = "Ticket nem található")
            }
    )
    public ResponseEntity<SupportTicketResponse> addMessage(@PathVariable("id") Long id,@Valid @RequestBody CreateTicketMessageRequest request) {
        return ResponseEntity.ok(
                supportTicketService.addMessage(id, request)
        );
    }
}
