package hu.finex.main.controller;

import hu.finex.main.dto.CreateSupportTicketRequest;
import hu.finex.main.dto.SupportTicketResponse;
import hu.finex.main.dto.UpdateSupportTicketStatusRequest;
import hu.finex.main.model.enums.TicketStatus;
import hu.finex.main.service.SupportTicketService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/support-tickets")
@RequiredArgsConstructor
@Tag(name = "Support Ticket API", description = "Ügyfélszolgálati ticketek kezelése")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping
    @Operation(summary = "Új support ticket létrehozása",responses = {
                    @ApiResponse(responseCode = "201", description = "Ticket létrehozva",content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet"),
                    @ApiResponse(responseCode = "409", description = "Már van nyitott ticket")
            }
    )
    public ResponseEntity<SupportTicketResponse> create(@Valid @RequestBody CreateSupportTicketRequest request) {
        SupportTicketResponse response = supportTicketService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Support ticket lekérdezése ID alapján",responses = {
                    @ApiResponse(responseCode = "200", description = "Siker",content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Ticket nem található")
            }
    )
    public ResponseEntity<SupportTicketResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(supportTicketService.getById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Egy felhasználó összes ticketje (lapozva)",responses = {
                    @ApiResponse(responseCode = "200", description = "Siker")
            }
    )
    public ResponseEntity<Page<SupportTicketResponse>> listByUser(@PathVariable Long userId,Pageable pageable) {
        return ResponseEntity.ok(
                supportTicketService.listByUser(userId, pageable)
        );
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Összes ticket adott státusszal",responses = {
                    @ApiResponse(responseCode = "200", description = "Siker")
            }
    )
    public ResponseEntity<Page<SupportTicketResponse>> listByStatus( @PathVariable TicketStatus status,Pageable pageable) {
        return ResponseEntity.ok(
                supportTicketService.listByStatus(status, pageable)
        );
    }

    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Felhasználó ticketjei adott státusszal",responses = {
                    @ApiResponse(responseCode = "200", description = "Siker")
            }
    )
    public ResponseEntity<Page<SupportTicketResponse>> listByUserAndStatus(@PathVariable Long userId,@PathVariable TicketStatus status,Pageable pageable) {
        return ResponseEntity.ok(
                supportTicketService.listByUserAndStatus(userId, status, pageable)
        );
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Support ticket státuszának módosítása",responses = {
                    @ApiResponse(responseCode = "200", description = "Státusz frissítve",content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Ticket nem található"),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet")
            }
    )
    public ResponseEntity<SupportTicketResponse> updateStatus(@PathVariable Long id,@Valid @RequestBody UpdateSupportTicketStatusRequest request) {
        return ResponseEntity.ok(
                supportTicketService.updateStatus(id, request)
        );
    }
}
