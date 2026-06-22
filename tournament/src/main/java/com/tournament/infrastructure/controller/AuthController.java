package com.tournament.infrastructure.controller;

import com.tournament.application.dto.request.LoginRequest;
import com.tournament.application.dto.request.RefreshTokenRequest;
import com.tournament.application.dto.request.SignUpRequest;
import com.tournament.application.dto.response.AuthResponse;
import com.tournament.application.service.AuthService;
import com.tournament.domain.entity.User;
import com.tournament.infrastructure.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody SignUpRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Cuenta creada exitosamente. ¡Bienvenido!"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(response, "Inicio de sesión exitoso"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refresh(request.getRefreshToken());

        return ResponseEntity.ok(
                ApiResponse.success(response, "Token renovado exitosamente"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        authService.logout(request.getRefreshToken());

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logoutAllSessions(
            @AuthenticationPrincipal User currentUser) {

        authService.logoutAllSessions(currentUser.getId());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(
            @AuthenticationPrincipal User currentUser) {

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.from(currentUser);

        return ResponseEntity.ok(
                ApiResponse.success(userInfo, "Perfil recuperado exitosamente"));
    }
}