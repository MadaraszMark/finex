package hu.finex.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.CreateCategoryRequest;
import hu.finex.main.dto.UpdateCategoryRequest;
import hu.finex.main.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – Category API", description = "Tranzakciós kategóriák kezelése (csak ADMIN)")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Új kategória létrehozása",responses = {
                    @ApiResponse(responseCode = "201", description = "Kategória létrehozva",content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet vagy foglalt név")
            }
    )
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Kategória módosítása",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres módosítás",content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Érvénytelen bemenet vagy foglalt név"),
                    @ApiResponse(responseCode = "404", description = "Kategória nem található")
            }
    )
    public ResponseEntity<CategoryResponse> update(@PathVariable("id") Long id,@Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Kategória törlése (csak ha egy tranzakció sem használja)",responses = {
                    @ApiResponse(responseCode = "204", description = "Sikeres törlés"),
                    @ApiResponse(responseCode = "400", description = "A kategória használatban van"),
                    @ApiResponse(responseCode = "404", description = "Kategória nem található")
            }
    )
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
