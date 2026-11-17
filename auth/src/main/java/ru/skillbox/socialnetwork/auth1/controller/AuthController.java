package ru.skillbox.socialnetwork.auth1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.socialnetwork.auth1.dto.AuthenticateRq;
import ru.skillbox.socialnetwork.auth1.dto.RefreshRequest;
import ru.skillbox.socialnetwork.auth1.dto.RegistrationDto;
import ru.skillbox.socialnetwork.auth1.dto.TokenResponse;
import ru.skillbox.socialnetwork.auth1.enums.TokenType;
import ru.skillbox.socialnetwork.auth1.service.AuthService;
import ru.skillbox.socialnetwork.auth1.service.JwtService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegistrationDto registrationDto
    ) {
        authService.registerNewUser(registrationDto);
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshRequest request
            ){
      TokenResponse body = authService.refreshToken(request);
        return ResponseEntity.ok(body);
    }
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody AuthenticateRq authenticateRq
    ){
        TokenResponse body = authService.login(authenticateRq);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validate(@RequestParam String token) {
        return ResponseEntity.ok(jwtService.isValid(token, TokenType.ACCESS));
    }

}
