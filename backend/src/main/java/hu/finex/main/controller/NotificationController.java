package hu.finex.main.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.NotificationResponse;
import hu.finex.main.dto.UnreadCountResponse;
import hu.finex.main.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "Értesítések (beérkező utalás, kamatjóváírás, biztonsági események, ügyfélszolgálati válasz)")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Saját értesítések (lapozva, a legújabb elöl)",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés")
            }
    )
    public ResponseEntity<Page<NotificationResponse>> listMine(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.listMine(pageable));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Olvasatlan értesítések száma (a csengő ikon jelvényéhez)",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = UnreadCountResponse.class)))
            }
    )
    public ResponseEntity<UnreadCountResponse> countUnread() {
        return ResponseEntity.ok(notificationService.countUnread());
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Értesítés olvasottra állítása",responses = {
                    @ApiResponse(responseCode = "200", description = "Olvasottra állítva",content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Értesítés nem található")
            }
    )
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable("id") Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Összes értesítés olvasottra állítása",responses = {
                    @ApiResponse(responseCode = "204", description = "Kész")
            }
    )
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
