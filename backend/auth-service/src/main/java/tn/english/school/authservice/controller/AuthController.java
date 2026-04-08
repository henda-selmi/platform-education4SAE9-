package tn.english.school.authservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.english.school.authservice.dto.AuthResponse;
import tn.english.school.authservice.dto.LoginRequest;
import tn.english.school.authservice.dto.RegisterRequest;
import tn.english.school.authservice.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(422)
                    .body(Map.of("errors", Map.of("password", new String[]{"Invalid email or password."})));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Stateless JWT — client simply discards the token
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }
}
