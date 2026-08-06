package com.school21.shopapi.controller;

import com.school21.shopapi.dto.auth.AuthLoginDto;
import com.school21.shopapi.dto.auth.AuthRegisterDto;
import com.school21.shopapi.dto.auth.AuthResetDto;
import com.school21.shopapi.dto.auth.AuthChangePasswordDto;
import com.school21.shopapi.service.AuthClient;
import io.grpc.StatusRuntimeException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthClient authClient;

    public AuthController(AuthClient authClient) {
        this.authClient = authClient;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRegisterDto dto) {
        try {
            String token = authClient.register(
                    dto.getEmail(),
                    dto.getFirstName(),
                    dto.getLastName(),
                    dto.getPhone(),
                    dto.getPassword()
            );
            return ResponseEntity.ok(Map.of("token", token));
        } catch (StatusRuntimeException e) {
            // Если Питонист вернул ошибку, берем его текст и возвращаем с кодом 400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getStatus().getDescription()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthLoginDto dto) {
        try {
            String token = authClient.login(dto.getEmail(), dto.getPassword());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (StatusRuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getStatus().getDescription()));
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@Valid @RequestBody AuthResetDto dto) {
        try {
            var response = authClient.resetPassword(dto.getEmail());
            return ResponseEntity.ok(Map.of("message", response.getMessage()));
        } catch (StatusRuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getStatus().getDescription()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader, 
            @Valid @RequestBody AuthChangePasswordDto dto) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Отсутствует или неверный токен"));
            }
            String token = authHeader.substring(7);
            var response = authClient.changePassword(token, dto.getOldPassword(), dto.getNewPassword());
            return ResponseEntity.ok(Map.of("success", true));
        } catch (StatusRuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getStatus().getDescription()));
        }
    }
}
