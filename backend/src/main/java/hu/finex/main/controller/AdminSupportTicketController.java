package hu.finex.main.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import hu.finex.main.dto.CreateTicketMessageRequest;
import hu.finex.main.dto.SupportTicketListItemResponse;
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

@RestController
@RequestMapping("/admin/support-tickets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – Support Ticket API", description = "Ügyfélszolgálati ticketek kezelése (csak ADMIN)")
public class AdminSupportTicketController {

    private final SupportTicketService supportTicketService;

    @GetMapping
    @Operation(summary = "Összes ticket, opcionálisan státusz szerint (lapozva)",responses = {
                    @ApiResponse(responseCode = "200", description = "Siker")
            }
    )
    public ResponseEntity<Page<SupportTicketListItemResponse>> listAll(@RequestParam(value = "status", required = false) TicketStatus status,
                                                                       @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(supportTicketService.listAll(status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ticket a teljes beszélgetéssel",responses = {
                    @ApiResponse(responseCode = "200", description = "Siker",content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Ticket nem található")
            }
    )
    public ResponseEntity<SupportTicketResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(supportTicketService.getById(id));
    }

    @PostMapping("/{id}/messages")
    @Operation(summary = "Válasz a ticketre (a nyitott ticket folyamatban állapotba kerül, a felhasználó értesítést kap)",responses = {
                    @ApiResponse(responseCode = "200", description = "Válasz elküldve",content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Ticket nem található")
            }
    )
    public ResponseEntity<SupportTicketResponse> reply(@PathVariable("id") Long id,@Valid @RequestBody CreateTicketMessageRequest request) {
        return ResponseEntity.ok(supportTicketService.reply(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Ticket státuszának módosítása",responses = {
                    @ApiResponse(responseCode = "200", description = "Státusz frissítve",content = @Content(schema = @Schema(implementation = SupportTicketResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Ticket nem található"),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet")
            }
    )
    public ResponseEntity<SupportTicketResponse> updateStatus(@PathVariable("id") Long id,@Valid @RequestBody UpdateSupportTicketStatusRequest request) {
        return ResponseEntity.ok(supportTicketService.updateStatus(id, request));
    }
}
