package com.ayawo.banque.ega.controllers;

import com.ayawo.banque.ega.dto.auth.AuthResponseDTO;
import com.ayawo.banque.ega.dto.auth.LoginRequestDTO;
import com.ayawo.banque.ega.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint de connexion
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {

        AuthResponseDTO response = authService.login(loginRequest);

        return ResponseEntity.ok(response);
    }
}