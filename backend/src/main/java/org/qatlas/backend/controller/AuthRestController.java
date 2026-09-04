package org.qatlas.backend.controller;

import org.qatlas.backend.security.AdminTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.qatlas.backend.Constants.SLASH;

@RestController
@RequestMapping(SLASH + "auth")
@Tag(name = "Auth", description = "Admin login for gated actions such as deleting executions")
public class AuthRestController {

    private final String adminUsername;
    private final String adminPassword;
    private final AdminTokenService tokenService;

    public AuthRestController(
            @Value("${app.security.admin-username}") final String adminUsername,
            @Value("${app.security.admin-password}") final String adminPassword,
            final AdminTokenService tokenService) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.tokenService = tokenService;
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token, String username) {}

    @PostMapping("/login")
    @Operation(summary = "Log in as the shared admin account", operationId = "adminLogin")
    public ResponseEntity<LoginResponse> login(@RequestBody final LoginRequest request) {
        if (request == null
                || !adminUsername.equals(request.username())
                || !adminPassword.equals(request.password())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        final String token = tokenService.issueToken(adminUsername);
        return ResponseEntity.ok(new LoginResponse(token, adminUsername));
    }

    @GetMapping("/me")
    @Operation(summary = "Check whether the current token is a valid admin session", operationId = "adminMe")
    public ResponseEntity<LoginResponse> me(@RequestHeader(value = "Authorization", required = false) final String authHeader) {
        final String token = extractBearerToken(authHeader);
        return tokenService.validate(token)
                .map(username -> ResponseEntity.ok(new LoginResponse(token, username)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private static String extractBearerToken(final String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring("Bearer ".length());
    }
}
