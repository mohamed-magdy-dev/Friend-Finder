package com.project._5.Friend_Finder.controller;

import com.project._5.Friend_Finder.dto.AuthResponse;
import com.project._5.Friend_Finder.dto.LoginRequest;
import com.project._5.Friend_Finder.dto.RegisterRequest;
import com.project._5.Friend_Finder.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS to make front communicate without any problems
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}