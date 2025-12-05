package hu.finex.main.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.CreateCategoryRequest;
import hu.finex.main.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Category API", description = "Tranzakciós kategóriák kezelése")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Új kategória létrehozása",responses = {
                    @ApiResponse(responseCode = "201", description = "Kategória létrehozva",content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet"),
                    @ApiResponse(responseCode = "409", description = "Kategória már létezik")
            }
    )
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Kategória lekérdezése ID alapján",responses = {
                    @ApiResponse(responseCode = "200", description = "Siker",content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Kategória nem található")
            }
    )
    public ResponseEntity<CategoryResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @GetMapping
    @Operation(summary = "Összes kategória listázása",responses = {
                    @ApiResponse(responseCode = "200", description = "Siker",content = @Content(schema = @Schema(implementation = CategoryResponse.class)))
            }
    )
    public ResponseEntity<List<CategoryResponse>> listAll() {
        return ResponseEntity.ok(categoryService.listAll());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Kategória törlése",responses = {
                    @ApiResponse(responseCode = "204", description = "Sikeres törlés"),
                    @ApiResponse(responseCode = "404", description = "Kategória nem található")
            }
    )
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
