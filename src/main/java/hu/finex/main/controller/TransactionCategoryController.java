package hu.finex.main.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.TransactionCategoryListItemResponse;
import hu.finex.main.dto.TransactionCategoryResponse;
import hu.finex.main.service.TransactionCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transaction-categories")
@RequiredArgsConstructor
@Tag(name = "Transaction Category API", description = "Tranzakciók és kategóriák kapcsolatának kezelése")
public class TransactionCategoryController {

    private final TransactionCategoryService service;

    @PostMapping("/{transactionId}/assign/{categoryId}")
    @Operation(summary = "Kategória hozzárendelése egy tranzakcióhoz",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres hozzárendelés",content = @Content(schema = @Schema(implementation = TransactionCategoryResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Tranzakció vagy kategória nem található"),
                    @ApiResponse(responseCode = "409", description = "Kapcsolat már létezik")
            }
    )
    public ResponseEntity<TransactionCategoryResponse> assign(@PathVariable("transactionId") Long transactionId,@PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(service.assignCategory(transactionId, categoryId));
    }

    @GetMapping("/transaction/{transactionId}")
    @Operation(summary = "Egy tranzakcióhoz tartozó kategóriák listázása",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres művelet",content = @Content(schema = @Schema(implementation = TransactionCategoryListItemResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Tranzakció nem található")
            }
    )
    public ResponseEntity<List<TransactionCategoryListItemResponse>> listByTransaction(@PathVariable("transactionId") Long transactionId) {
        return ResponseEntity.ok(service.listByTransaction(transactionId));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Egy kategóriához tartozó tranzakciók listázása",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = TransactionCategoryResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Kategória nem található")
            }
    )
    public ResponseEntity<List<TransactionCategoryResponse>> listByCategory(@PathVariable("categoryId") Long categoryId) {
        return ResponseEntity.ok(service.listByCategory(categoryId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Tranzakció–kategória kapcsolat törlése",responses = {
                    @ApiResponse(responseCode = "204", description = "Sikeres törlés"),
                    @ApiResponse(responseCode = "404", description = "Kapcsolat nem található")
            }
    )
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteRelation(id);
        return ResponseEntity.noContent().build();
    }
}
