package hu.finex.main.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import hu.finex.main.dto.UpdateUserRoleRequest;
import hu.finex.main.dto.UpdateUserStatusRequest;
import hu.finex.main.dto.UserDetailsResponse;
import hu.finex.main.dto.UserListItemResponse;
import hu.finex.main.dto.UserResponse;
import hu.finex.main.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin – User API", description = "Felhasználók kezelése (csak ADMIN)")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Felhasználók keresése névre vagy e-mail címre (lapozva)",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés"),
                    @ApiResponse(responseCode = "403", description = "Nincs admin jogosultság")
            }
    )
    public ResponseEntity<Page<UserListItemResponse>> search(@RequestParam(value = "search", required = false) String search,
                                                             @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(userService.search(search, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Egy felhasználó teljes áttekintése (számlák, kártyák, megtakarítások)",responses = {
                    @ApiResponse(responseCode = "200", description = "Sikeres lekérdezés",content = @Content(schema = @Schema(implementation = UserDetailsResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Felhasználó nem található")
            }
    )
    public ResponseEntity<UserDetailsResponse> getDetails(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getDetails(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Felhasználó letiltása vagy feloldása",responses = {
                    @ApiResponse(responseCode = "200", description = "Státusz módosítva",content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "A saját fiók nem tiltható le"),
                    @ApiResponse(responseCode = "404", description = "Felhasználó nem található")
            }
    )
    public ResponseEntity<UserResponse> updateStatus(@PathVariable("id") Long id,@Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(userService.updateStatus(id, request));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Felhasználó szerepkörének módosítása",responses = {
                    @ApiResponse(responseCode = "200", description = "Szerepkör módosítva",content = @Content(schema = @Schema(implementation = UserResponse.class))),
                    @ApiResponse(responseCode = "400", description = "A saját szerepkör nem módosítható"),
                    @ApiResponse(responseCode = "404", description = "Felhasználó nem található")
            }
    )
    public ResponseEntity<UserResponse> updateRole(@PathVariable("id") Long id,@Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }
}
